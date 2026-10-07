<script lang="ts">
  import { enhance } from '$app/forms';
  import { resolve } from '$app/paths';

  import { Button } from '#lib/components/ui/button/index.js';
  import * as Card from '#lib/components/ui/card/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { Label } from '#lib/components/ui/label/index.js';

  let { form } = $props();
</script>

<div class="mx-auto max-w-sm px-4 py-12">
  <Card.Root>
    <Card.Header>
      <Card.Title>Login</Card.Title>
    </Card.Header>
    <Card.Content>
      <form method="post" use:enhance class="flex flex-col gap-4">
        <div class="flex flex-col gap-2">
          <Label for="login">Username or email</Label>
          <Input id="login" name="login" autocomplete="username" value={form?.login ?? ''} required />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="password">Password</Label>
          <Input id="password" name="password" type="password" autocomplete="current-password" required />
        </div>
        {#if form?.message}
          <p class="text-sm text-destructive">{form.message}</p>
        {/if}
        <Button type="submit">Login</Button>
      </form>
    </Card.Content>
    <Card.Footer>
      <p class="text-sm text-muted-foreground">
        No account?
        <a href={resolve('register')} class="underline">Register</a>
      </p>
    </Card.Footer>
  </Card.Root>
</div>
