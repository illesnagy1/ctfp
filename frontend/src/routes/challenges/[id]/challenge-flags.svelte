<script lang="ts">
  import FlagIcon from '@lucide/svelte/icons/flag';
  import Trash2Icon from '@lucide/svelte/icons/trash-2';
  import { toast } from 'svelte-sonner';
  import type { Infer, SuperValidated } from 'sveltekit-superforms';

  import { Badge } from '#lib/components/ui/badge/index.js';
  import { Button } from '#lib/components/ui/button/index.js';
  import { confirmDelete } from '#lib/components/ui/confirm-delete-dialog/index.js';
  import { submitAction } from '#lib/form-action.js';
  import { m } from '#lib/paraglide/messages.js';
  import type { createFlagSchema } from '#lib/schemas/flag.js';
  import type { components } from '#lib/types/api.js';

  import CreateFlagDialog from './create-flag-dialog.svelte';
  import { flagValueLabel } from './flag-labels.js';

  type Flag = components['schemas']['FlagResponse'];

  /** Admins only: the flags are the answers. */
  let { flags, createForm }: { flags: Flag[]; createForm: SuperValidated<Infer<typeof createFlagSchema>> } = $props();

  function askToDelete(flag: Flag) {
    confirmDelete({
      title: m.challenge_flags_delete_title(),
      description: m.challenge_flags_delete_description({ pattern: flag.pattern }),
      onConfirm: async () => {
        await submitAction('?/deleteFlag', { id: String(flag.id) });
        toast.success(m.challenge_flags_deleted());
      },
    });
  }
</script>

<section class="flex flex-col gap-3" aria-labelledby="challenge-flags-title">
  <div class="flex items-center justify-between gap-2">
    <h2 id="challenge-flags-title" class="flex items-center gap-2 text-sm font-medium">
      <FlagIcon class="size-4" />
      {m.challenge_flags_title()}
    </h2>
    <CreateFlagDialog data={createForm} />
  </div>

  {#if flags.length === 0}
    <!-- A challenge without flags can't be solved. -->
    <p class="text-sm text-muted-foreground">{m.challenge_flags_empty()}</p>
  {:else}
    <ul class="divide-y rounded-md border">
      {#each flags as flag (flag.id)}
        <li class="flex items-center gap-3 p-3 text-sm">
          <div class="flex min-w-0 flex-1 flex-col gap-1">
            <code class="truncate font-medium" title={flag.pattern}>{flag.pattern}</code>
            <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
              <span>{flagValueLabel(flag)}</span>
              {#if !flag.isCaseSensitive}
                <Badge variant="outline">{m.challenge_flags_case_insensitive()}</Badge>
              {/if}
            </div>
          </div>
          <Button
            variant="ghost"
            size="icon-sm"
            aria-label={m.challenge_flags_delete_title()}
            onclick={() => askToDelete(flag)}
          >
            <Trash2Icon />
          </Button>
        </li>
      {/each}
    </ul>
  {/if}
</section>
