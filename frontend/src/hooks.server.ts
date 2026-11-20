import { sequence, type Handle } from '@sveltejs/kit/hooks';
import { createInitialModeExpression } from 'mode-watcher';

import { getTextDirection } from '#lib/paraglide/runtime.js';
import { paraglideMiddleware } from '#lib/paraglide/server.js';
import { SESSION_COOKIE, getSession } from '#lib/server/session.js';

/** mode-watcher's no-flash theme script, run from app.html so it carries the CSP nonce (see +layout.svelte). */
const modeScript = createInitialModeExpression();

const handleParaglide: Handle = ({ event, resolve }) =>
  // `event.request` is read-only in SvelteKit 3. Paraglide's request only differs with the `url` strategy, which
  // isn't used; URL de-localization is handled by `reroute` in hooks.ts.
  paraglideMiddleware(event.request, ({ locale }) =>
    resolve(event, {
      transformPageChunk: ({ html }) =>
        html
          .replace('%paraglide.lang%', locale)
          .replace('%paraglide.dir%', getTextDirection(locale))
          .replace('%ctfp.modeScript%', modeScript),
    }),
  );

const handleSession: Handle = async ({ event, resolve }) => {
  const sessionId = event.cookies.get(SESSION_COOKIE);
  const session = sessionId ? await getSession(sessionId) : undefined;

  if (session) {
    event.locals.session = session;
    event.locals.user = session.user;
  } else if (sessionId) {
    event.cookies.delete(SESSION_COOKIE, { path: '/' });
  }

  return resolve(event);
};

/** Request bodies outside uploads; BODY_SIZE_LIMIT (compose.yml) is sized for uploads and applies to every route. */
const MAX_BODY_BYTES = 1024 * 1024;
/** Challenge file uploads, which stream through to the api (routes/challenges/[id]/files), with an optional locale. */
const UPLOAD_PATH = /^(\/[a-z]{2}(-[A-Za-z]+)?)?\/challenges\/\d+\/files\/?$/;

const handleBodyLimit: Handle = ({ event, resolve }) => {
  const { method, headers } = event.request;
  if (method !== 'GET' && method !== 'HEAD' && !UPLOAD_PATH.test(event.url.pathname)) {
    const length = headers.get('content-length');
    // A body without a length (chunked) can't be checked up front, so only uploads may send one.
    if (length === null ? headers.has('transfer-encoding') : Number(length) > MAX_BODY_BYTES) {
      return new Response('Request body too large', { status: 413 });
    }
  }
  return resolve(event);
};

/**
 * Pages and data carry the user, their flags, and on /reset-password a token, so no cache (browser back/forward,
 * a shared proxy) may keep them; responses that set their own Cache-Control (file downloads) keep it. Logging out
 * also clears the browser's cache for the site (not its cookies: logout deletes the session one, the language stays).
 */
const handleCaching: Handle = async ({ event, resolve }) => {
  const response = await resolve(event);
  if (!response.headers.has('cache-control')) response.headers.set('cache-control', 'no-store');
  if (
    event.request.method === 'POST' &&
    event.url.pathname.replace(/^\/[a-z]{2}(-[A-Za-z]+)?(?=\/)/, '') === '/logout'
  ) {
    response.headers.set('clear-site-data', '"cache"');
  }
  return response;
};

export const handle: Handle = sequence(handleBodyLimit, handleCaching, handleParaglide, handleSession);
