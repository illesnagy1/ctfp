<script lang="ts">
  import { Badge } from '#lib/components/ui/badge/index.js';

  let { categories }: { categories: string[] } = $props();
</script>

<div class="flex flex-wrap gap-1">
  {#each categories as category (category)}
    <Badge variant="outline">{category}</Badge>
  {/each}
</div>
