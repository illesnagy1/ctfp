import { describe, expect, it } from 'vitest';

import { isClientError, isRedirection, isServerError, isSuccess, status, statusClass } from './http';

describe('status classes', () => {
  it('classifies registered codes', () => {
    expect(isSuccess(status.NO_CONTENT)).toBe(true);
    expect(isRedirection(status.SEE_OTHER)).toBe(true);
    expect(isClientError(status.CONFLICT)).toBe(true);
    expect(isServerError(status.BAD_GATEWAY)).toBe(true);
    expect(isClientError(status.INTERNAL_SERVER_ERROR)).toBe(false);
  });

  it("doesn't guess for unregistered codes", () => {
    expect(statusClass(599)).toBeUndefined();
  });
});
