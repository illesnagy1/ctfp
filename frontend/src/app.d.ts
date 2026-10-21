import type { Session, SessionUser } from '#lib/server/session.js';

// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
  namespace App {
    interface Locals {
      user?: SessionUser;
      session?: Session;
    }

    // interface Error {}
    interface PageData {
      /** The browser's time zone from its cookie, if valid (see src/lib/time.ts). */
      timeZone?: string;
    }
    // interface PageState {}
    // interface Platform {}
  }
}

export {};
