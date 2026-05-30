<script lang="ts">
	import { resolve } from '$app/paths';
	import './layout.css';
	import favicon from '$lib/assets/favicon.svg';
	import { Button } from '$lib/components/ui/button';
	import { LogIn, UserPlus } from '@lucide/svelte';
	import { ThemeSelector } from '$lib/components/ui/theme-selector';
	import { LanguageSwitcher, type Language } from '$lib/components/ui/language-switcher';

	let { children } = $props();

	const languages: Language[] = [
		{ code: 'en', label: 'English' },
		{ code: 'hu', label: 'Magyar' },
	];
</script>

<svelte:head><link rel="icon" href={favicon} /></svelte:head>

<header>
	<nav>
		<Button href={resolve("/")}>CTF+</Button>
		<Button href={resolve("/register")}><UserPlus /> Register</Button>
		<Button href={resolve("/login")}><LogIn /> Login</Button>
		<LanguageSwitcher {languages} />
		<ThemeSelector />
	</nav>
</header>

{@render children()}

<footer>
	<p>CTF+</p>
</footer>
