import { defineConfig } from '@hey-api/openapi-ts';

// Zod schemas of the api's DTOs, limits included, for the forms to build on (see src/lib/schemas). Part of
// `bun run gen:api`; the output is committed, since the frontend's container build can't read ../api.
export default defineConfig({
  input: '../api/openapi.json',
  output: 'src/lib/types/zod',
  plugins: [{ name: 'zod', definitions: true, requests: false, responses: false }],
});
