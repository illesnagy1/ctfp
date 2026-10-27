<script lang="ts">
  import { resolve } from '$app/paths';
  import CopyIcon from '@lucide/svelte/icons/copy';
  import DownloadIcon from '@lucide/svelte/icons/download';
  import Trash2Icon from '@lucide/svelte/icons/trash-2';
  import prettyBytes from 'pretty-bytes';
  import { toast } from 'svelte-sonner';

  import { Button } from '#lib/components/ui/button/index.js';
  import { confirmDelete } from '#lib/components/ui/confirm-delete-dialog/index.js';
  import { submitAction } from '#lib/form-action.js';
  import { m } from '#lib/paraglide/messages.js';
  import { getLocale } from '#lib/paraglide/runtime.js';
  import type { components } from '#lib/types/api.js';

  let { file, isAdmin }: { file: components['schemas']['FileResponse']; isAdmin: boolean } = $props();

  function askToDelete() {
    confirmDelete({
      title: m.challenge_files_delete_title(),
      description: m.challenge_files_delete_description({ filename: file.filename }),
      onConfirm: async () => {
        await submitAction('?/deleteFile', { id: String(file.id) });
        toast.success(m.challenge_files_deleted({ filename: file.filename }));
      },
    });
  }

  async function copyHash() {
    try {
      await navigator.clipboard.writeText(file.sha256);
      toast.success(m.challenge_files_hash_copied());
    } catch {
      toast.error(m.challenge_files_hash_copy_failed());
    }
  }
</script>

<li class="flex items-center gap-3 p-3 text-sm">
  <div class="flex min-w-0 flex-1 flex-col gap-0.5">
    <a
      href={resolve('/files/[id]/download', { id: String(file.id) })}
      download={file.filename}
      class="flex items-center gap-1.5 truncate font-medium hover:underline"
    >
      <DownloadIcon class="size-4 shrink-0" />
      <span class="truncate">{file.filename}</span>
    </a>
    <div class="flex items-center gap-2 text-xs text-muted-foreground">
      <span>{prettyBytes(file.size, { locale: getLocale() })}</span>
      <span aria-hidden="true">·</span>
      <code class="truncate" title={file.sha256}>
        SHA-256 {file.sha256.slice(0, 8)}…{file.sha256.slice(-8)}
      </code>
      <Button variant="ghost" size="icon-sm" aria-label={m.challenge_files_copy_hash()} onclick={copyHash}>
        <CopyIcon />
      </Button>
    </div>
  </div>
  {#if isAdmin}
    <Button
      variant="ghost"
      size="icon-sm"
      aria-label={m.challenge_files_delete({ filename: file.filename })}
      onclick={askToDelete}
    >
      <Trash2Icon />
    </Button>
  {/if}
</li>
