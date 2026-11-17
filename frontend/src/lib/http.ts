import { status } from 'http-status';

/** Named HTTP status codes, e.g. `status.NOT_FOUND`, and their names, messages and classes (`status['404_CLASS']`). */
export { status };

const byKey: Readonly<Record<string, unknown>> = status;

/** The code's class (`status.classes.*`, e.g. '4xx'); undefined for codes outside the IANA registry. */
export function statusClass(code: number): string | undefined {
  const value = byKey[`${code}_CLASS`];
  return typeof value === 'string' ? value : undefined;
}

export const isInformational = (code: number) => statusClass(code) === status.classes.INFORMATIONAL;
export const isSuccess = (code: number) => statusClass(code) === status.classes.SUCCESSFUL;
export const isRedirection = (code: number) => statusClass(code) === status.classes.REDIRECTION;
export const isClientError = (code: number) => statusClass(code) === status.classes.CLIENT_ERROR;
export const isServerError = (code: number) => statusClass(code) === status.classes.SERVER_ERROR;
