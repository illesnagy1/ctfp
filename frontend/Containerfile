FROM oven/bun:1-slim AS base
WORKDIR /usr/src/app

FROM base AS install
WORKDIR /temp/dev
COPY package.json bun.lock* /temp/dev/
RUN bun install --frozen-lockfile

WORKDIR /temp/prod
COPY package.json bun.lock* /temp/prod/
RUN bun install --frozen-lockfile --production

FROM base AS prerelease
COPY --from=install /temp/dev/node_modules node_modules
COPY . .
RUN bun run build

FROM base AS release
COPY --from=install --chown=bun:bun /temp/prod/node_modules node_modules
COPY --from=prerelease --chown=bun:bun /usr/src/app/build .
COPY --from=prerelease --chown=bun:bun /usr/src/app/package.json .

USER bun
EXPOSE 3000/tcp
ENV PORT=3000
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD bun -e "fetch('http://localhost:'+process.env.PORT).then(r=>process.exit(r.ok?0:1)).catch(()=>process.exit(1))"
ENTRYPOINT [ "bun", "index.js" ]
