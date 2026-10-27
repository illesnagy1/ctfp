<script lang="ts">
  import { refreshAll } from '$app/navigation';
  import { resolve } from '$app/paths';
  import PaperclipIcon from '@lucide/svelte/icons/paperclip';
  import UploadIcon from '@lucide/svelte/icons/upload';
  import { toast } from 'svelte-sonner';
  import { on } from 'svelte/events';

  import * as FileDropZone from '#lib/components/ui/file-drop-zone/index.js';
  import { Progress } from '#lib/components/ui/progress/index.js';
  import { m } from '#lib/paraglide/messages.js';
  import type { components } from '#lib/types/api.js';

  import ChallengeFile from './challenge-file.svelte';

  let {
    challengeId,
    files,
    isAdmin,
  }: {
    challengeId: number;
    files: components['schemas']['FileResponse'][];
    isAdmin: boolean;
  } = $props();

  /** The file being uploaded and how far along it is (0–1). */
  let uploading = $state<{ name: string; progress: number }>();

  /** XHR rather than fetch: fetch can't report upload progress. */
  function upload(file: File) {
    return new Promise<void>((done, fail) => {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', resolve('/challenges/[id]/files', { id: String(challengeId) }));
      // SvelteKit answers errors from endpoints as JSON then, not as an HTML page.
      xhr.setRequestHeader('Accept', 'application/json');
      on(xhr.upload, 'progress', (e) => {
        if (e instanceof ProgressEvent && e.lengthComputable && uploading) uploading.progress = e.loaded / e.total;
      });
      on(xhr, 'load', () => {
        if (xhr.status === 201) return done();
        let message: unknown;
        try {
          message = JSON.parse(xhr.responseText)?.message;
        } catch {
          // Not JSON, e.g. the frontend server's own size limit.
        }
        fail(new Error(typeof message === 'string' ? message : xhr.statusText || m.form_action_failed()));
      });
      on(xhr, 'error', () => fail(new Error(m.form_action_failed())));

      const body = new FormData();
      body.append('file', file);
      xhr.send(body);
    });
  }

  /** Called by the drop zone with dropped or picked files; it stays disabled until this returns. */
  async function uploadFiles(selected: File[]) {
    // One at a time, so the progress is meaningful and one failure doesn't stop the rest.
    for (const file of selected) {
      uploading = { name: file.name, progress: 0 };
      try {
        // eslint-disable-next-line no-await-in-loop -- sequential on purpose, see above
        await upload(file);
        toast.success(m.challenge_files_uploaded({ filename: file.name }));
      } catch (error) {
        toast.error(m.challenge_files_upload_failed({ filename: file.name, reason: (error as Error).message }));
      }
    }
    uploading = undefined;
    await refreshAll();
  }
</script>

{#if files.length > 0 || isAdmin}
  <section class="flex flex-col gap-3" aria-labelledby="challenge-files-title">
    <div class="flex items-center justify-between gap-2">
      <h2 id="challenge-files-title" class="flex items-center gap-2 text-sm font-medium">
        <PaperclipIcon class="size-4" />
        {m.challenge_files_title()}
      </h2>
    </div>

    {#if isAdmin}
      <!-- The api enforces the size limit (UPLOADS_MAX_BYTES), so none is set here. -->
      <FileDropZone.Root onUpload={uploadFiles}>
        <FileDropZone.Trigger>
          <div
            class="flex flex-col items-center justify-center gap-2 rounded-lg border border-dashed p-6 text-center text-sm text-muted-foreground transition-colors group-aria-disabled/file-drop-zone-trigger:opacity-50 hover:cursor-pointer hover:bg-accent/25 group-aria-disabled/file-drop-zone-trigger:hover:cursor-not-allowed"
          >
            <UploadIcon class="size-6" />
            <span class="font-medium">{m.challenge_files_drop()}</span>
          </div>
        </FileDropZone.Trigger>
      </FileDropZone.Root>
    {/if}

    {#if uploading}
      <div class="flex flex-col gap-1 text-sm" role="status">
        <span class="truncate text-muted-foreground">
          {m.challenge_files_uploading({ filename: uploading.name, percent: Math.round(uploading.progress * 100) })}
        </span>
        <Progress value={uploading.progress * 100} aria-label={uploading.name} />
      </div>
    {/if}

    {#if files.length === 0}
      <p class="text-sm text-muted-foreground">{m.challenge_files_empty()}</p>
    {:else}
      <ul class="divide-y rounded-md border">
        {#each files as file (file.id)}
          <ChallengeFile {file} {isAdmin} />
        {/each}
      </ul>
    {/if}
  </section>
{/if}
