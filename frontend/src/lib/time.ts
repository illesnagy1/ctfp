/** Holds the browser's IANA time zone, so the server can format times in it too. */
export const TIME_ZONE_COOKIE = 'tz';

/** Whether `value` is a time zone `Intl` accepts; cookie values are user-controlled. */
export function isTimeZone(value: string | undefined): value is string {
  if (!value) return false;
  try {
    // Throws a RangeError for unknown zones.
    return !!new Intl.DateTimeFormat('en', { timeZone: value }).resolvedOptions().timeZone;
  } catch {
    return false;
  }
}

export function browserTimeZone() {
  return Intl.DateTimeFormat().resolvedOptions().timeZone;
}
