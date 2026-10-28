<script lang="ts">
  import { BasicForm } from '@sjsf/form';
  import { createMeta, setupSvelteKitForm } from '@sjsf/sveltekit3/client';
  import { adapt } from '@sjsf/zod4-validator/classic';
  import { toast } from 'svelte-sonner';

  import { m } from '#lib/paraglide/messages.js';
  import { settingsSchema, settingsUiSchema } from '#lib/schemas/settings.js';
  import * as defaults from '#lib/sjsf/defaults.js';

  import type { ActionData, PageData } from './$types';

  const meta = createMeta<ActionData, PageData>().settingsForm;

  // Every field comes from the api's Settings schema; see src/lib/schemas/settings.ts.
  const { form } = setupSvelteKitForm(meta, {
    ...defaults,
    ...adapt(settingsSchema),
    uiSchema: settingsUiSchema(),
    onSuccess: (result) => {
      if (result.type === 'success') toast.success(m.settings_saved());
    },
    onFailure: () => toast.error(m.settings_save_failed()),
  });
</script>

<div class="mx-auto w-full max-w-2xl px-4 py-6">
  <h1 class="pb-4 text-2xl font-semibold tracking-tight">{m.settings_title()}</h1>
  <BasicForm {form} method="POST" novalidate />
</div>
