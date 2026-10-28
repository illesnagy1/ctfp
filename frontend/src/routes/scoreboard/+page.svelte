<script lang="ts">
  import { resolve } from '$app/paths';

  import * as Table from '#lib/components/ui/table/index.js';
  import { m } from '#lib/paraglide/messages.js';

  let { data } = $props();

  // Comes ranked: most points first, then whoever reached them first. Only exact ties share a rank (1, 2, 2, 4, …).
  const ranked = $derived(
    data.entries.map((entry, _, all) => {
      const first = all.findIndex((e) => e.points === entry.points && e.lastSolveAt === entry.lastSolveAt);
      return { entry, rank: first + 1 };
    }),
  );
</script>

<div class="mx-auto w-full max-w-4xl px-4 py-6">
  <h1 class="pb-4 text-2xl font-semibold tracking-tight">{m.scoreboard_title()}</h1>

  <Table.Root>
    <Table.Caption>{m.scoreboard_caption()}</Table.Caption>
    <Table.Header>
      <Table.Row>
        <Table.Head>{m.scoreboard_rank()}</Table.Head>
        <Table.Head>{m.scoreboard_name()}</Table.Head>
        <Table.Head>{m.scoreboard_country()}</Table.Head>
        <Table.Head class="text-right">{m.scoreboard_solves()}</Table.Head>
        <Table.Head class="text-right">{m.scoreboard_points()}</Table.Head>
      </Table.Row>
    </Table.Header>
    <Table.Body>
      {#each ranked as { entry, rank } (entry.userId)}
        <Table.Row>
          <Table.Cell><span class="font-medium">{rank}</span></Table.Cell>
          <Table.Cell>
            <a href={resolve('/users/[id]', { id: String(entry.userId) })} class="underline-offset-4 hover:underline">
              {entry.username}
            </a>
          </Table.Cell>
          <Table.Cell>{entry.country ?? '-'}</Table.Cell>
          <Table.Cell class="text-right">{entry.solves}</Table.Cell>
          <Table.Cell class="text-right">{entry.points}</Table.Cell>
        </Table.Row>
      {:else}
        <Table.Row>
          <Table.Cell colspan={5} class="h-24 text-center">{m.scoreboard_empty()}</Table.Cell>
        </Table.Row>
      {/each}
    </Table.Body>
  </Table.Root>
</div>
