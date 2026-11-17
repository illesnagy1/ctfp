import { superValidate } from 'sveltekit-superforms';
import { zod4 } from 'sveltekit-superforms/adapters';
import { describe, expect, it } from 'vitest';
import { z } from 'zod';

import { apiFormError } from './form';

const schema = z.object({ username: z.string(), email: z.string() });
const badRequest = () => new Response(null, { status: 400 });

describe('apiFormError', () => {
  it("puts a problem's field errors on the matching fields", async () => {
    const form = await superValidate({ username: 'x', email: 'p@example.com' }, zod4(schema));
    const problem = {
      title: 'Bad Request',
      status: 400,
      detail: 'username must be at least 3 characters',
      errors: [
        { pointer: '#/username', detail: 'username must be at least 3 characters' },
        { pointer: '#/notOnTheForm', detail: 'ignored' },
      ],
    };

    const failure = apiFormError(form, badRequest(), problem, 'Test');
    expect(failure.status).toBe(400);
    expect(form.errors.username).toEqual(['username must be at least 3 characters']);
    expect(form.message).toBeUndefined();
  });

  it('shows other problems as the form message', async () => {
    const form = await superValidate({ username: 'player', email: 'p@example.com' }, zod4(schema));
    apiFormError(
      form,
      new Response(null, { status: 409 }),
      { title: 'Conflict', status: 409, detail: 'Taken' },
      'Test',
    );
    expect(form.message).toBe('Taken');
  });
});
