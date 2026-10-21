/** Holds the browser's IANA time zone, so the server can format times in it too. */
export const TIME_ZONE_COOKIE = 'tz';

/** Whether `value` is a time zone `Intl` accepts; cookie values are user-controlled. */
export function isTimeZone(value: string | undefined): value is string {
  if (!value) return false;
  try {
    new Intl.DateTimeFormat('en', { timeZone: value });
    return true;
  } catch {
    return false;
  }
}

export function browserTimeZone() {
  return Intl.DateTimeFormat().resolvedOptions().timeZone;
}
