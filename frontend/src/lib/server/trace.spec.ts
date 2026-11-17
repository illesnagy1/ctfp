import { ROOT_CONTEXT, TraceFlags, propagation, trace } from '@opentelemetry/api';
import { W3CTraceContextPropagator } from '@opentelemetry/core';
import { afterEach, describe, expect, it } from 'vitest';

import { injectTraceContext } from './trace';

describe('injectTraceContext', () => {
  afterEach(() => propagation.disable());

  it('adds the span as a W3C traceparent', () => {
    propagation.setGlobalPropagator(new W3CTraceContextPropagator());
    const span = {
      traceId: '0af7651916cd43dd8448eb211c80319c',
      spanId: 'b7ad6b7169203331',
      traceFlags: TraceFlags.SAMPLED,
    };
    const headers = new Headers();
    injectTraceContext(headers, trace.setSpanContext(ROOT_CONTEXT, span));
    expect(headers.get('traceparent')).toBe('00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01');
  });

  it('adds nothing without tracing set up', () => {
    const headers = new Headers();
    injectTraceContext(headers);
    expect([...headers.keys()]).toEqual([]);
  });
});
