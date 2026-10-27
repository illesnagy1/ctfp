import prettyBytes from 'pretty-bytes';

import { getLocale } from '#lib/paraglide/runtime.js';

import DragOverlay from './file-drop-zone-drag-overlay.svelte';
import Textarea from './file-drop-zone-textarea.svelte';
import Trigger from './file-drop-zone-trigger.svelte';
import Root from './file-drop-zone.svelte';
import type { FileDropZoneDragOverlayProps, FileDropZoneRootProps, FileRejectedReason } from './types';

export function displaySize(bytes: number): string {
  return prettyBytes(bytes, { locale: getLocale() });
}

// Utilities for working with file sizes
export const BYTE = 1;
export const KILOBYTE = 1000;
export const MEGABYTE = 1000 * KILOBYTE;
export const GIGABYTE = 1000 * MEGABYTE;

// utilities for limiting accepted files
export const ACCEPT_IMAGE = 'image/*';
export const ACCEPT_VIDEO = 'video/*';
export const ACCEPT_AUDIO = 'audio/*';

export {
  Root,
  Trigger,
  Textarea,
  DragOverlay,
  type FileDropZoneRootProps,
  type FileDropZoneDragOverlayProps,
  type FileRejectedReason,
};
