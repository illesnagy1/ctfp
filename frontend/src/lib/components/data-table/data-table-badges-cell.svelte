<script lang="ts">
  import { Badge } from '#lib/components/ui/badge/index.js';

  /** A row of outline badges, e.g. a challenge's categories or a user's role and statuses. */
  let { badges }: { badges: string[] } = $props();
</script>

<div class="flex flex-wrap gap-1">
  {#each badges as badge (badge)}
    <Badge variant="outline">{badge}</Badge>
  {/each}
</div>
