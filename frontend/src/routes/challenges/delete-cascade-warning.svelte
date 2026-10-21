<script lang="ts">
  import TriangleAlertIcon from '@lucide/svelte/icons/triangle-alert';

  import { m } from '#lib/paraglide/messages.js';
</script>

<!-- Mirrors the api's ON DELETE CASCADE on flags, hints (with unlocks) and submissions. -->
<div role="note" class="flex gap-3 rounded-md border border-destructive/50 bg-destructive/10 p-3 text-sm">
  <TriangleAlertIcon class="mt-0.5 size-4 shrink-0 text-destructive" />
  <div>
    <p class="font-medium text-destructive">{m.challenges_delete_cascade_title()}</p>
    <ul class="mt-1 list-disc pl-4">
      <li>{m.challenges_delete_cascade_flags()}</li>
      <li>{m.challenges_delete_cascade_hints()}</li>
      <li>{m.challenges_delete_cascade_submissions()}</li>
    </ul>
  </div>
</div>
