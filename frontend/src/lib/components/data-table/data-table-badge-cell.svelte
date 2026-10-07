<script lang="ts">
  import { Badge } from '#lib/components/ui/badge/index.js';

  /** Truncated text with an optional outline badge in front, e.g. a task title and its label. */
  let { value, badge }: { value: string; badge?: string } = $props();
</script>

<div class="flex space-x-2">
  {#if badge}
    <Badge variant="outline">{badge}</Badge>
  {/if}
  <span class="max-w-125 truncate font-medium">
    {value}
  </span>
</div>
