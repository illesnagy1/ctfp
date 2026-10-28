<script lang="ts">
  /** A cell that links to the row's page, e.g. a name to its details. */
  let { href, text }: { href: string; text: string } = $props();
</script>

<a {href} class="font-medium hover:underline">{text}</a>
