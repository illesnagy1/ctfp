<script lang="ts">
  import { enhance } from '$app/forms';
  import PlusIcon from '@lucide/svelte/icons/plus';
  import { toast } from 'svelte-sonner';

  import { Button } from '#lib/components/ui/button/index.js';
  import { Checkbox } from '#lib/components/ui/checkbox/index.js';
  import * as Dialog from '#lib/components/ui/dialog/index.js';
  import { Input } from '#lib/components/ui/input/index.js';
  import { Label } from '#lib/components/ui/label/index.js';
  import { TagsInput } from '#lib/components/ui/tags-input/index.js';
  import { Textarea } from '#lib/components/ui/textarea/index.js';
  import { actionError } from '#lib/form-action.js';
  import { m } from '#lib/paraglide/messages.js';

  /** Categories already in use, offered as suggestions. */
  let { knownCategories = [] }: { knownCategories?: string[] } = $props();

  let open = $state(false);
  let pending = $state(false);
  let publishAt = $state('');
  let categories = $state<string[]>([]);

  /** Current local time in the `datetime-local` input format. */
  function nowLocal() {
    const now = new Date();
    return new Date(now.getTime() - now.getTimezoneOffset() * 60_000).toISOString().slice(0, 16);
  }

  function onOpenChange(value: boolean) {
    if (!value) return;
    publishAt = nowLocal();
    categories = [];
  }
</script>

<Dialog.Root bind:open {onOpenChange}>
  <Dialog.Trigger>
    {#snippet child({ props })}
      <Button {...props}><PlusIcon />{m.challenges_create()}</Button>
    {/snippet}
  </Dialog.Trigger>
  <Dialog.Content class="sm:max-w-lg">
    <Dialog.Header>
      <Dialog.Title>{m.challenges_create()}</Dialog.Title>
      <Dialog.Description>{m.challenges_create_description()}</Dialog.Description>
    </Dialog.Header>
    <form
      method="post"
      action="?/create"
      class="flex flex-col gap-4"
      use:enhance={({ formData }) => {
        pending = true;
        const title = String(formData.get('title'));
        return async ({ result, update }) => {
          pending = false;
          const error = actionError(result);
          if (error) {
            // Stays open with what was typed, so it can be fixed.
            toast.error(error);
            return;
          }
          await update();
          if (result.type === 'success') {
            toast.success(m.challenges_created({ title }));
            open = false;
          }
        };
      }}
    >
      <div class="flex flex-col gap-2">
        <Label for="challenge-title">{m.challenges_field_title()}</Label>
        <Input id="challenge-title" name="title" maxlength={255} required />
      </div>
      <div class="flex flex-col gap-2">
        <Label for="challenge-description">{m.challenges_field_description()}</Label>
        <Textarea id="challenge-description" name="description" rows={4} />
      </div>
      <div class="flex flex-col gap-2">
        <Label for="challenge-categories">{m.challenges_field_categories()}</Label>
        <!-- No `name` here: it would land on the inner text input and submit the half-typed tag. -->
        <TagsInput
          id="challenge-categories"
          bind:value={categories}
          suggestions={knownCategories}
          placeholder={m.challenges_field_categories_placeholder()}
        />
        {#each categories as category (category)}
          <input type="hidden" name="categories" value={category} />
        {/each}
      </div>
      <div class="flex flex-col gap-2">
        <Label for="challenge-publish-at">{m.challenges_field_publish_at()}</Label>
        <Input id="challenge-publish-at" type="datetime-local" bind:value={publishAt} required />
        <!-- The server can't know the browser's time zone, so send an absolute time. -->
        <input type="hidden" name="publishAt" value={publishAt && new Date(publishAt).toISOString()} />
      </div>
      <div class="grid grid-cols-2 gap-4">
        <div class="flex flex-col gap-2">
          <Label for="challenge-max-attempts">{m.challenges_field_max_attempts()}</Label>
          <Input id="challenge-max-attempts" name="maxAttempts" type="number" min={0} step={1} value={0} required />
        </div>
        <div class="flex flex-col gap-2">
          <Label for="challenge-time-limit">{m.challenges_field_time_limit()}</Label>
          <Input id="challenge-time-limit" name="timeLimit" type="number" min={0} step={1} value={0} required />
        </div>
        <p class="col-span-2 -mt-2 text-xs text-muted-foreground">{m.challenges_field_limit_hint()}</p>
      </div>
      <div class="flex items-center gap-2">
        <Checkbox id="challenge-visible" name="isVisible" checked />
        <Label for="challenge-visible">{m.challenges_field_visible()}</Label>
      </div>
      <Dialog.Footer>
        <Dialog.Close>
          {#snippet child({ props })}
            <Button variant="outline" {...props}>{m.challenges_create_cancel()}</Button>
          {/snippet}
        </Dialog.Close>
        <Button type="submit" disabled={pending}>{m.challenges_create_submit()}</Button>
      </Dialog.Footer>
    </form>
  </Dialog.Content>
</Dialog.Root>
