import { sequence, type Handle } from '@sveltejs/kit/hooks';

import { getTextDirection } from '#lib/paraglide/runtime.js';
import { paraglideMiddleware } from '#lib/paraglide/server.js';
import { SESSION_COOKIE, getSession } from '#lib/server/session.js';

const handleParaglide: Handle = ({ event, resolve }) =>
  // `event.request` is read-only in SvelteKit 3. Paraglide's request only differs with the `url` strategy, which
  // isn't used; URL de-localization is handled by `reroute` in hooks.ts.
  paraglideMiddleware(event.request, ({ locale }) =>
    resolve(event, {
      transformPageChunk: ({ html }) =>
        html.replace('%paraglide.lang%', locale).replace('%paraglide.dir%', getTextDirection(locale)),
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

export const handle: Handle = sequence(handleParaglide, handleSession);
