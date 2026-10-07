<script lang="ts">
  import { Badge } from '#lib/components/ui/badge/index.js';

  import { labels } from '../data/data.js';

  let { value, labelValue }: { value: string; labelValue: string } = $props();

  const label = $derived(labels.find((l) => l.value === labelValue));
</script>

<div class="flex space-x-2">
  {#if label}
    <Badge variant="outline">{label.label}</Badge>
  {/if}
  <span class="max-w-125 truncate font-medium">
    {value}
  </span>
</div>
