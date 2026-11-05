# sv

Everything you need to build a Svelte project, powered by [`sv`](https://github.com/sveltejs/cli).

## Creating a project

If you're seeing this, you've probably already done this step. Congrats!

```sh
# create a new project
npx sv create my-app
```

To recreate this project with the same configuration:

```sh
# recreate this project
bun x sv@0.15.3 create --template minimal --types ts --add prettier eslint vitest="usages:unit,component" tailwindcss="plugins:typography,forms" better-auth="demo:password" mdsvex paraglide="languageTags:en, hu+demo:no" drizzle="database:sqlite+sqlite:libsql" --install bun frontend
```

## Developing

Once you've created a project and installed dependencies with `npm install` (or `pnpm install` or `yarn`), start a development server:

```sh
npm run dev

# or start the server and open the app in a new browser tab
npm run dev -- --open
```

## Building

To create a production version of your app:

```sh
npm run build
```

You can preview the production build with `npm run preview`.

> To deploy your app, you may need to install an [adapter](https://svelte.dev/docs/kit/adapters) for your target environment.

## TODO

- https://app-privacy-policy-generator.nisrulz.com/
- one source of truth for input limits (username length, password length, etc.), now written twice: in the api's
  `UserValidation.kt` and in `src/lib/schemas/`
  - api: put the limits on the request DTOs as `@JsonSchema.MinLength` / `MaxLength` / `Pattern` / `Format` /
    `Minimum` annotations (Ktor 3.6, `io.ktor.openapi.JsonSchema`); they show up in `openapi.json`
  - api: they only document, so add one generic RequestValidation rule (marker interface on the DTOs) that reads the
    annotations from the serializer descriptor and checks them, answering 400 via StatusPages; drop the hand-written
    checks, keep the ones annotations can't express (e.g. username taken)
  - api: remove the template `validate<String>` "Hello" rule in `plugins/RequestValidation.kt`
  - frontend: generate base zod schemas from `openapi.json` (e.g. `@hey-api/openapi-ts` zod plugin) next to
    `gen:api`; build the form schemas on them (`.extend()` / `.refine()` for `confirmPassword` etc.) and translate the
    generic errors with one `z.config({ customError })`
  - decide where trimming happens: the check sees the body as sent
