import { context, propagation, type Context } from '@opentelemetry/api';

/**
 * Adds W3C Trace Context headers (`traceparent`, `tracestate`) for the current span, SvelteKit's for this load or
 * action, so the api's spans join the same trace. Does nothing unless tracing is set up (src/instrumentation.server.ts).
 */
export function injectTraceContext(headers: Headers, from: Context = context.active()) {
  propagation.inject(from, headers, { set: (carrier, key, value) => carrier.set(key, value) });
}
