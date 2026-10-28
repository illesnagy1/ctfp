import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';
import { describe, expect, it } from 'vitest';

import { registerSchema } from './auth';
import { createChallengeSchema } from './challenge';
import { createFlagSchema } from './flag';
import { idsSchema } from './ids';
import { settingsSchema, settingsUiSchema } from './settings';
import { createUserSchema, setBannedSchema } from './user';

const issuePaths = (result: { success: boolean; error?: { issues: { path: PropertyKey[] }[] } }) =>
  result.error?.issues.map((issue) => issue.path.join('.')) ?? [];

describe('registerSchema', () => {
  const valid = {
    username: 'player.one',
    email: 'player@example.com',
    password: 'correct-horse',
    confirmPassword: 'correct-horse',
    registrationCode: '',
  };

  it('accepts a valid registration', () => {
    expect(registerSchema.safeParse(valid).success).toBe(true);
  });

  it('reports a password mismatch on the confirmation field', () => {
    expect(issuePaths(registerSchema.safeParse({ ...valid, confirmPassword: 'other-horse' }))).toEqual([
      'confirmPassword',
    ]);
  });

  it.each([
    ['username', 'ab'],
    ['username', 'has space'],
    ['email', 'not-an-email'],
    ['password', 'short'],
  ])('rejects an invalid %s', (field, value) => {
    expect(
      issuePaths(registerSchema.safeParse({ ...valid, [field]: value, confirmPassword: valid.password })),
    ).toContain(field);
  });
});

describe('createChallengeSchema', () => {
  const valid = {
    title: '  Warmup  ',
    description: '',
    categories: ['web', 'web', 'crypto'],
    publishAt: '2026-10-27T09:00:00.000Z',
    maxAttempts: 0,
    timeLimit: 3600,
    isVisible: true,
  };

  // Bound form fields need real defaults; a `.transform()` leaves them undefined.
  it('has superforms defaults for every field', async () => {
    const { data } = await superValidate(zod4(createChallengeSchema));
    expect(Object.values(data)).not.toContain(undefined);
    expect(data.categories).toEqual([]);
  });

  it('trims the title and drops duplicate categories', () => {
    const result = createChallengeSchema.parse(valid);
    expect(result.title).toBe('Warmup');
    expect(result.categories).toEqual(['web', 'crypto']);
  });

  it.each([
    ['title', '   '],
    ['publishAt', '2026-10-27T09:00'],
    ['maxAttempts', -1],
    ['timeLimit', 1.5],
  ])('rejects an invalid %s', (field, value) => {
    expect(issuePaths(createChallengeSchema.safeParse({ ...valid, [field]: value }))).toContain(field);
  });
});

describe('action payloads', () => {
  it('parses comma-separated ids', () => {
    expect(idsSchema.parse({ ids: '1,2,3' }).ids).toEqual([1, 2, 3]);
    expect(idsSchema.safeParse({ ids: '1,x' }).success).toBe(false);
  });

  it('parses the banned flag', () => {
    expect(setBannedSchema.parse({ banned: 'true' }).banned).toBe(true);
    expect(setBannedSchema.parse({ banned: 'false' }).banned).toBe(false);
  });

  it('defaults new users to verified, visible players', () => {
    const user = createUserSchema.parse({ username: 'player', email: 'p@example.com', password: 'correct-horse' });
    expect(user).toMatchObject({ role: 'USER', isVerified: true, isHidden: false });
  });
});

describe('createFlagSchema', () => {
  it('has superforms defaults for every field', async () => {
    const { data } = await superValidate(zod4(createFlagSchema));
    expect(data).toMatchObject({ pattern: '', isCaseSensitive: true, baseValue: 100, decayFun: 'static' });
  });

  it('only checks the minimum against the value when the value decays', () => {
    const flag = { pattern: 'x', isCaseSensitive: true, baseValue: 50, decayValue: 5, minValue: 80 };
    expect(createFlagSchema.safeParse({ ...flag, decayFun: 'static' }).success).toBe(true);
    expect(issuePaths(createFlagSchema.safeParse({ ...flag, decayFun: 'linear' }))).toEqual(['minValue']);
  });
});

describe('settingsSchema', () => {
  const valid = { ctfName: 'CTFP', registrationOpen: true, maxTeamSize: 4 };

  it("keeps the api's limits from the generated JSON Schema", () => {
    expect(settingsSchema.safeParse(valid).success).toBe(true);
    expect(issuePaths(settingsSchema.safeParse({ ...valid, ctfName: '' }))).toEqual(['ctfName']);
    expect(issuePaths(settingsSchema.safeParse({ ...valid, ctfName: 'x'.repeat(65) }))).toEqual(['ctfName']);
    expect(issuePaths(settingsSchema.safeParse({ ...valid, maxTeamSize: 0 }))).toEqual(['maxTeamSize']);
    expect(issuePaths(settingsSchema.safeParse({ ...valid, maxTeamSize: 1.5 }))).toEqual(['maxTeamSize']);
  });

  it('labels every setting', () => {
    const ui = settingsUiSchema();
    expect(Object.keys(ui)).toEqual(Object.keys(valid));
    expect(ui).toMatchObject({ maxTeamSize: { 'ui:options': { title: 'Max team size' } } });
  });
});
