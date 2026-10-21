import { TIME_ZONE_COOKIE, isTimeZone } from '#lib/time.js';

import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = ({ locals, cookies }) => {
  const timeZone = cookies.get(TIME_ZONE_COOKIE);
  return { user: locals.user, timeZone: isTimeZone(timeZone) ? timeZone : undefined };
};
