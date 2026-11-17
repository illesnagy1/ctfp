import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-proto';
import { NodeSDK } from '@opentelemetry/sdk-node';

// Loaded by SvelteKit before the app. Like the api (plugins/Monitoring.kt), tracing is on only with an OTLP endpoint;
// the exporter and service name come from the standard OTEL_* env vars. SvelteKit's own spans (vite.config.ts,
// `tracing.server`) are then exported, and api calls carry their trace context (src/lib/server/trace.ts).
if (process.env.OTEL_EXPORTER_OTLP_ENDPOINT || process.env.OTEL_EXPORTER_OTLP_TRACES_ENDPOINT) {
  const sdk = new NodeSDK({ traceExporter: new OTLPTraceExporter() });
  sdk.start();
  // Flushes pending spans when the container stops.
  process.once('SIGTERM', () => void sdk.shutdown());
}
