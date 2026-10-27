<script lang="ts">
  import { resolve } from '$app/paths';

  let { id, title }: { id: number; title: string } = $props();
</script>

<a href={resolve('/challenges/[id]', { id: String(id) })} class="font-medium hover:underline">{title}</a>
