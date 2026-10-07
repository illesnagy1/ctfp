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
      <Card.Title>Register</Card.Title>
    </Card.Header>
    <Card.Content>
      <form method="post" use:enhance class="flex flex-col gap-4">
        <div class="flex flex-col gap-2">
          <Label for="username">Username</Label>
          <Input
            id="username"
            name="username"
            autocomplete="username"
            minlength={3}
            maxlength={64}
            value={form?.username ?? ''}
            required
          />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="email">Email</Label>
          <Input id="email" name="email" type="email" autocomplete="email" value={form?.email ?? ''} required />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="password">Password</Label>
          <Input
            id="password"
            name="password"
            type="password"
            autocomplete="new-password"
            minlength={8}
            maxlength={128}
            required
          />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="confirmPassword">Confirm password</Label>
          <Input id="confirmPassword" name="confirmPassword" type="password" autocomplete="new-password" required />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="registrationCode">Registration code (optional)</Label>
          <Input id="registrationCode" name="registrationCode" value={form?.registrationCode ?? ''} />
        </div>
        {#if form?.message}
          <p class="text-sm text-destructive">{form.message}</p>
        {/if}
        <Button type="submit">Register</Button>
      </form>
    </Card.Content>
    <Card.Footer>
      <p class="text-sm text-muted-foreground">
        Already registered?
        <a href={resolve('login')} class="underline">Login</a>
      </p>
    </Card.Footer>
  </Card.Root>
</div>
