<script lang="ts">
  import EditUserDialog from '#lib/components/users/edit-user-dialog.svelte';
  import UserProfile from '#lib/components/users/user-profile.svelte';

  let { data } = $props();
</script>

<!-- Only for your own profile, or any as an admin. -->
{#snippet edit()}
  {#if data.editForm}
    <EditUserDialog data={data.editForm} admin={data.admin} self={data.self} />
  {/if}
{/snippet}

<div class="mx-auto max-w-2xl px-4 py-12">
  <UserProfile user={data.profile} actions={data.editForm ? edit : undefined} />
</div>
