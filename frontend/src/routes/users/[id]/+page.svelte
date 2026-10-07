<script lang="ts">
  import UserProfile from '#lib/components/users/user-profile.svelte';

  let { data } = $props();
</script>

<div class="mx-auto max-w-2xl px-4 py-12">
  <UserProfile user={data.profile} />
</div>
