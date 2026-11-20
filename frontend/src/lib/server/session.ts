import { dev } from '$app/env';
import { REDIS_URL, SESSION_IDLE_MINUTES } from '$app/env/private';
import type { Cookies } from '@sveltejs/kit';
import { createClient } from 'redis';
import { z } from 'zod';

const sessionUserSchema = z.object({
  id: z.number(),
  username: z.string(),
  email: z.string(),
  role: z.enum(['ADMIN', 'USER']),
});

const sessionSchema = z.object({
  token: z.jwt(),
  user: sessionUserSchema,
  expiresAt: z.number(),
});

export type SessionUser = z.infer<typeof sessionUserSchema>;
export type Session = z.infer<typeof sessionSchema>;

interface SessionStore {
  get(key: string): Promise<Session | undefined>;
  /** Stores [session] until [ttlSeconds] from now. */
  set(key: string, session: Session, ttlSeconds: number): Promise<void>;
  /** Moves an existing session's end to [ttlSeconds] from now. */
  touch(key: string, ttlSeconds: number): Promise<void>;
  delete(key: string): Promise<void>;
}

class MemoryStore implements SessionStore {
  private readonly sessions = new Map<string, { session: Session; until: number }>();

  constructor() {
    setInterval(() => {
      const now = Date.now() / 1000;
      for (const [key, { until }] of this.sessions) {
        if (until <= now) this.sessions.delete(key);
      }
    }, 60_000).unref?.();
  }

  async get(key: string) {
    const entry = this.sessions.get(key);
    if (entry && entry.until <= Date.now() / 1000) {
      this.sessions.delete(key);
      return undefined;
    }
    return entry?.session;
  }

  async set(key: string, session: Session, ttlSeconds: number) {
    this.sessions.set(key, { session, until: Date.now() / 1000 + ttlSeconds });
  }

  async touch(key: string, ttlSeconds: number) {
    const entry = this.sessions.get(key);
    if (entry) entry.until = Date.now() / 1000 + ttlSeconds;
  }

  async delete(key: string) {
    this.sessions.delete(key);
  }
}

class RedisStore implements SessionStore {
  private readonly client: ReturnType<typeof createClient>;
  private readonly ready: Promise<unknown>;

  constructor(url: string) {
    this.client = createClient({ url });
    this.client.on('error', (error) => console.error('Redis session store error', error));
    this.ready = this.client.connect();
  }

  async get(key: string) {
    await this.ready;
    const value = await this.client.get(`session:${key}`);
    if (!value) return undefined;
    // Treat a stored session that no longer matches the schema as logged out.
    const parsed = sessionSchema.safeParse(JSON.parse(value));
    return parsed.success ? parsed.data : undefined;
  }

  async set(key: string, session: Session, ttlSeconds: number) {
    await this.ready;
    await this.client.set(`session:${key}`, JSON.stringify(session), { expiration: { type: 'EX', value: ttlSeconds } });
  }

  async touch(key: string, ttlSeconds: number) {
    await this.ready;
    await this.client.expire(`session:${key}`, ttlSeconds);
  }

  async delete(key: string) {
    await this.ready;
    await this.client.del(`session:${key}`);
  }
}

// Kept on globalThis so the in-memory store survives Vite hot reloads in dev.
const globalStore = globalThis as typeof globalThis & { ctfpSessionStore?: SessionStore };
const store = (globalStore.ctfpSessionStore ??= REDIS_URL ? new RedisStore(REDIS_URL) : new MemoryStore());

// `__Host-`: the browser only accepts it over HTTPS, for this exact host and path `/`, so a subdomain or plain-HTTP
// page can't plant or overwrite it (session fixation). Dev runs on plain HTTP, where the prefix isn't allowed.
export const SESSION_COOKIE = dev ? 'session' : '__Host-session';

/** Seconds [session] may stay unused: the idle timeout, but never past its token's expiry. */
function ttlFor(session: Session) {
  return Math.min(SESSION_IDLE_MINUTES * 60, Math.floor(session.expiresAt - Date.now() / 1000));
}

/** Sessions are stored under a hash of the cookie value, so a leaked store dump can't be replayed as cookies. */
async function storeKey(sessionId: string) {
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(sessionId));
  return Buffer.from(digest).toString('base64url');
}

export async function createSession(cookies: Cookies, session: Session) {
  // The cookie lasts as long as the token; the store entry ends sooner when idle (see getSession).
  const ttlSeconds = Math.floor(session.expiresAt - Date.now() / 1000);
  if (ttlSeconds <= 0) return;

  // A new login replaces the browser's previous session instead of leaving it valid in the store.
  await deleteSession(cookies);
  const sessionId = Buffer.from(crypto.getRandomValues(new Uint8Array(32))).toString('base64url');
  await store.set(await storeKey(sessionId), session, ttlFor(session));

  cookies.set(SESSION_COOKIE, sessionId, {
    path: '/',
    httpOnly: true,
    secure: !dev,
    sameSite: 'lax',
    maxAge: ttlSeconds,
  });
}

/** The session for a cookie value, if it exists and hasn't been idle too long; each use restarts the idle timer. */
export async function getSession(sessionId: string): Promise<Session | undefined> {
  const key = await storeKey(sessionId);
  const session = await store.get(key);
  if (session) await store.touch(key, ttlFor(session));
  return session;
}

export async function deleteSession(cookies: Cookies) {
  const sessionId = cookies.get(SESSION_COOKIE);
  if (sessionId) await store.delete(await storeKey(sessionId));
  cookies.delete(SESSION_COOKIE, { path: '/' });
}
