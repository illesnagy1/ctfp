import { getTextDirection } from '$lib/paraglide/runtime';
import { paraglideMiddleware } from '$lib/paraglide/server';
import { SESSION_COOKIE, getSession } from '$lib/server/session';
import type { Handle } from '@sveltejs/kit';
import { sequence } from '@sveltejs/kit/hooks';

const handleParaglide: Handle = ({ event, resolve }) =>
  paraglideMiddleware(event.request, ({ request, locale }) => {
    event.request = request;

    return resolve(event, {
      transformPageChunk: ({ html }) =>
        html.replace('%paraglide.lang%', locale).replace('%paraglide.dir%', getTextDirection(locale)),
    });
  });

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
