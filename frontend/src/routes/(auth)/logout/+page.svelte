<script lang="ts">
  import { Button } from '#lib/components/ui/button/index.js';
  import * as Card from '#lib/components/ui/card/index.js';
  import { m } from '#lib/paraglide/messages.js';
</script>

<div class="mx-auto max-w-sm px-4 py-12">
  <Card.Root>
    <Card.Header>
      <Card.Title>{m.nav_logout()}</Card.Title>
      <Card.Description>{m.auth_logout_question()}</Card.Description>
    </Card.Header>
    <Card.Content>
      <form method="post">
        <Button type="submit" class="w-full">{m.nav_logout()}</Button>
      </form>
    </Card.Content>
  </Card.Root>
</div>
