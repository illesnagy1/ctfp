import type { FormState, ResolveFieldType } from '@sjsf/form';
import '@sjsf/shadcn4-theme/extra-widgets/textarea-include';
import { getSimpleSchemaType, isFixedItems } from '@sjsf/form/core';
import { createFocusOnFirstError } from '@sjsf/form/focus-on-first-error';

// import "@sjsf/form/fields/extra/aggregated-include";
// import "@sjsf/form/fields/extra/array-files-include";
// import "@sjsf/form/fields/extra/array-native-files-include";
// import "@sjsf/form/fields/extra/array-tags-include";
// import "@sjsf/form/fields/extra/boolean-select-include";
// import "@sjsf/form/fields/extra/enum-include";
// import "@sjsf/form/fields/extra/file-include";
// import "@sjsf/form/fields/extra/multi-enum-include";
// import "@sjsf/form/fields/extra/object-key-enum-include";
// import "@sjsf/form/fields/extra/remote-enum-include";
// import "@sjsf/form/fields/extra/unknown-native-file-include";
// https://x0k.dev/svelte-jsonschema-form/guides/fields-resolution/
export function resolver<T>(_ctx: FormState<T>): ResolveFieldType {
  return ({ schema }) => {
    if (schema.oneOf !== undefined) {
      return 'oneOfField';
    }

    if (schema.anyOf !== undefined) {
      return 'anyOfField';
    }

    const type = getSimpleSchemaType(schema);

    if (type === 'array') {
      return isFixedItems(schema) ? 'tupleField' : 'arrayField';
    }

    return `${type}Field`;
  };
}

export { translation } from '@sjsf/form/translations/en';
export { createFormMerger as merger } from '@sjsf/form/mergers/modern';
export { createFormIdBuilder as idBuilder } from '@sjsf/sveltekit3';

// import "@sjsf/shadcn4-theme/extra-widgets/checkboxes-include";
// import "@sjsf/shadcn4-theme/extra-widgets/combobox-include";
// import "@sjsf/shadcn4-theme/extra-widgets/date-picker-include";
// import "@sjsf/shadcn4-theme/extra-widgets/date-range-picker-include";
// import "@sjsf/shadcn4-theme/extra-widgets/file-include";
// import "@sjsf/shadcn4-theme/extra-widgets/multi-select-include";
// import "@sjsf/shadcn4-theme/extra-widgets/radio-buttons-include";
// import "@sjsf/shadcn4-theme/extra-widgets/radio-include";
// import "@sjsf/shadcn4-theme/extra-widgets/range-slider-include";
// import "@sjsf/shadcn4-theme/extra-widgets/range-include";
// import "@sjsf/shadcn4-theme/extra-widgets/switch-include";
// import "@sjsf-lab/shadcn-extras-theme/extra-widgets/file-drop-zone-include";
// import "@sjsf-lab/shadcn-extras-theme/extra-widgets/ip-v4-address-input-include";
// import "@sjsf-lab/shadcn-extras-theme/extra-widgets/nlp-date-input-include";
// import "@sjsf-lab/shadcn-extras-theme/extra-widgets/password-include";
// import "@sjsf-lab/shadcn-extras-theme/extra-widgets/phone-input-include";
// import "@sjsf-lab/shadcn-extras-theme/extra-widgets/star-rating-include";
// import "@sjsf-lab/shadcn-extras-theme/extra-widgets/tags-input-include";
export { theme } from '@sjsf-lab/shadcn-extras-theme';

export { icons } from '@sjsf/lucide-icons';
export const onSubmitError = createFocusOnFirstError();
