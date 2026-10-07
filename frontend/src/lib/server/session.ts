import { dev } from '$app/env';
import { REDIS_URL } from '$app/env/private';
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
  set(key: string, session: Session, ttlSeconds: number): Promise<void>;
  delete(key: string): Promise<void>;
}

class MemoryStore implements SessionStore {
  private readonly sessions = new Map<string, Session>();

  constructor() {
    setInterval(() => {
      const now = Date.now() / 1000;
      for (const [key, session] of this.sessions) {
        if (session.expiresAt <= now) this.sessions.delete(key);
      }
    }, 60_000).unref?.();
  }

  async get(key: string) {
    const session = this.sessions.get(key);
    if (session && session.expiresAt <= Date.now() / 1000) {
      this.sessions.delete(key);
      return undefined;
    }
    return session;
  }

  async set(key: string, session: Session) {
    this.sessions.set(key, session);
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

  async delete(key: string) {
    await this.ready;
    await this.client.del(`session:${key}`);
  }
}

// Kept on globalThis so the in-memory store survives Vite hot reloads in dev.
const globalStore = globalThis as typeof globalThis & { ctfpSessionStore?: SessionStore };
const store = (globalStore.ctfpSessionStore ??= REDIS_URL ? new RedisStore(REDIS_URL) : new MemoryStore());

export const SESSION_COOKIE = 'session';

/** Sessions are stored under a hash of the cookie value, so a leaked store dump can't be replayed as cookies. */
async function storeKey(sessionId: string) {
  const digest = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(sessionId));
  return Buffer.from(digest).toString('base64url');
}

export async function createSession(cookies: Cookies, session: Session) {
  const ttlSeconds = Math.floor(session.expiresAt - Date.now() / 1000);
  if (ttlSeconds <= 0) return;

  const sessionId = Buffer.from(crypto.getRandomValues(new Uint8Array(32))).toString('base64url');
  await store.set(await storeKey(sessionId), session, ttlSeconds);

  cookies.set(SESSION_COOKIE, sessionId, {
    path: '/',
    httpOnly: true,
    secure: !dev,
    sameSite: 'lax',
    maxAge: ttlSeconds,
  });
}

export async function getSession(sessionId: string): Promise<Session | undefined> {
  return store.get(await storeKey(sessionId));
}

export async function deleteSession(cookies: Cookies) {
  const sessionId = cookies.get(SESSION_COOKIE);
  if (sessionId) await store.delete(await storeKey(sessionId));
  cookies.delete(SESSION_COOKIE, { path: '/' });
}
