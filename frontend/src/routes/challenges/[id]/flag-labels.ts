import { m } from '#lib/paraglide/messages.js';
import type { decayFunctions } from '#lib/schemas/flag.js';
import type { components } from '#lib/types/api.js';

type Decay = (typeof decayFunctions)[number];

/** The decay function's name in the current locale; call it where it renders. */
export function decayLabel(decay: Decay) {
  return {
    static: m.challenge_flags_decay_static,
    linear: m.challenge_flags_decay_linear,
    logarithmic: m.challenge_flags_decay_logarithmic,
    parabolic: m.challenge_flags_decay_parabolic,
  }[decay]();
}

/** E.g. "100 points" or "100 → 10 points, linear, −5 per solve". */
export function flagValueLabel(flag: components['schemas']['FlagResponse']) {
  if (flag.decayFun === 'static') return m.challenge_flags_value_static({ value: flag.baseValue });
  return m.challenge_flags_value_decaying({
    value: flag.baseValue,
    min: flag.minValue ?? 0,
    decay: flag.decayValue ?? 0,
    function: decayLabel(flag.decayFun),
  });
}
