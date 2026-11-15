---
title: Getting started
description: Run CTF+ locally.
---

CTF+ uses [mise](https://mise.jdx.dev) for tools and tasks.

## Requirements

- [mise](https://mise.jdx.dev/getting-started.html)
- [Podman](https://podman.io) for the full stack (dev uses embedded PostgreSQL)

## Development

```sh
mise install      # dev tools and git hooks
mise run install  # project dependencies
mise run dev      # api and the frontend dev server (not containerized)
```

## Full stack (with podman)

```sh
JWT_SECRET=$(openssl rand -hex 32) mise run up
```

## Useful tasks

| Task | What it does |
| --- | --- |
| `mise run gen:api` | Regenerate `openapi.json` and the frontend API types |
| `mise run lint` | Run every linter |
| `mise run test` | Run api and frontend tests |
| `mise run docs` | Run this documentation site |

See `mise tasks` for more tasks.
