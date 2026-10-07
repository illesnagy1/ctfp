import { defineEnvVars } from '@sveltejs/kit/env';

export const variables = defineEnvVars({
  // Backend api, called server-side only. Unset or empty falls back to the local dev api.
  API_URL: { schema: (input) => input || 'http://localhost:8080' },
  // Session store. Unset or empty keeps sessions in memory (see src/lib/server/session.ts).
  REDIS_URL: { schema: (input) => input || undefined },
});
