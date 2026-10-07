<script lang="ts">
  import { Button } from '$lib/components/ui/button';
  import * as Card from '$lib/components/ui/card';
</script>

<div class="mx-auto max-w-sm px-4 py-12">
  <Card.Root>
    <Card.Header>
      <Card.Title>Logout</Card.Title>
      <Card.Description>Do you want to log out?</Card.Description>
    </Card.Header>
    <Card.Content>
      <form method="post">
        <Button type="submit" class="w-full">Logout</Button>
      </form>
    </Card.Content>
  </Card.Root>
</div>
