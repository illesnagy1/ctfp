import { defineConfig } from '@hey-api/openapi-ts';

// The api's types, a function per endpoint (sdk.gen.ts), the fetch client, and zod schemas of its DTOs with their limits
// (see src/lib/schemas). Part of `bun run gen:api`; the output is committed, since the frontend's container build can't
// read ../api.
export default defineConfig({
  input: '../api/openapi.json',
  output: 'src/lib/api',
  plugins: [
    '@hey-api/typescript',
    '@hey-api/client-fetch',
    // No default client: every call passes the session's own (see src/lib/server/api.ts), so none goes out without it.
    { name: '@hey-api/sdk', client: false },
    { name: 'zod', definitions: true, requests: false, responses: false },
  ],
});
