<script lang="ts">
  import '../app.css';
  import { onMount } from 'svelte';
  import { isAuthenticated, checkAuth, clearAuthToken, user } from '$lib/auth';
  import { fetchDashboardSummary } from '$lib/store';
  import { goto } from '$app/navigation';
  import { page } from '$app/stores';
  import { browser } from '$app/environment';

  // 특정 페이지 접근 제한 로직 수정 (랜딩 페이지 +page.svelte 는 누구나 접근 가능)
  $: if (browser && !$isAuthenticated && 
         $page.route.id !== '/' && 
         $page.route.id !== '/login' && 
         $page.route.id !== '/signup') {
    goto('/login');
  }

  onMount(async () => {
    checkAuth();
    if (isAuthenticated) {
      fetchDashboardSummary();
    }
  });

  function logout() {
    clearAuthToken();
    goto('/');
  }

  let isAssetsOpen = false;
</script>

<div class="min-h-screen bg-slate-50/50 dark:bg-slate-950 flex flex-col">
  <header class="sticky top-0 z-50 w-full border-b bg-white/80 dark:bg-slate-950/80 backdrop-blur-xl transition-all">
    <div class="container mx-auto flex h-16 items-center justify-between px-6">
      <div class="flex items-center gap-10">
          <a href="/" class="flex items-center space-x-2 group">
            <div class="bg-primary p-1.5 rounded-lg transition-transform group-hover:scale-110 shadow-lg shadow-primary/20">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10"/></svg>
            </div>
            <span class="font-black text-xl tracking-tight text-slate-900 dark:text-white uppercase">k-secure</span>
          </a>
        
        {#if $isAuthenticated}
          <nav class="hidden md:flex items-center gap-1">
            <a href="/" class="px-4 py-2 text-sm font-semibold rounded-lg transition-colors {$page.route.id === '/' ? 'bg-primary/10 text-primary' : 'text-slate-600 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-900'}">Dashboard</a>
            
            <div class="relative">
              <button 
                on:click={() => isAssetsOpen = !isAssetsOpen}
                class="flex items-center gap-1 px-4 py-2 text-sm font-semibold rounded-lg transition-colors {isAssetsOpen || $page.route.id?.includes('assets') ? 'bg-slate-100 text-slate-900 dark:bg-slate-900 dark:text-white' : 'text-slate-600 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-900'}"
              >
                Assets
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="ml-1 {isAssetsOpen ? 'rotate-180' : ''} transition-transform"><path d="m6 9 6 6 6-6"/></svg>
              </button>
              
              {#if isAssetsOpen}
                <div class="absolute top-full left-0 mt-2 w-52 rounded-xl border bg-white dark:bg-slate-950 p-2 shadow-2xl animate-in fade-in zoom-in-95 ring-1 ring-black/5">
                  <a href="/assets/pods" on:click={() => isAssetsOpen = false} class="flex items-center px-3 py-2 text-sm font-medium rounded-lg hover:bg-slate-100 dark:hover:bg-slate-900 transition-colors">Pods</a>
                  <a href="/assets/deployments" on:click={() => isAssetsOpen = false} class="flex items-center px-3 py-2 text-sm font-medium rounded-lg hover:bg-slate-100 dark:hover:bg-slate-900 transition-colors">Deployments</a>
                  <a href="/assets/services" on:click={() => isAssetsOpen = false} class="flex items-center px-3 py-2 text-sm font-medium rounded-lg hover:bg-slate-100 dark:hover:bg-slate-900 transition-colors">Services</a>
                  <a href="/assets/namespaces" on:click={() => isAssetsOpen = false} class="flex items-center px-3 py-2 text-sm font-medium rounded-lg hover:bg-slate-100 dark:hover:bg-slate-900 transition-colors">Namespaces</a>
                  <a href="/assets/nodes" on:click={() => isAssetsOpen = false} class="flex items-center px-3 py-2 text-sm font-medium rounded-lg hover:bg-slate-100 dark:hover:bg-slate-900 transition-colors">Nodes</a>
                  <a href="/assets/events" on:click={() => isAssetsOpen = false} class="flex items-center px-3 py-2 text-sm font-medium rounded-lg hover:bg-slate-100 dark:hover:bg-slate-900 transition-colors">Events</a>
                </div>
              {/if}
            </div>

            <a href="/alerts" class="px-4 py-2 text-sm font-semibold rounded-lg transition-colors {$page.route.id?.includes('alerts') ? 'bg-primary/10 text-primary' : 'text-slate-600 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-900'}">Alerts</a>
            <a href="/policies" class="px-4 py-2 text-sm font-semibold rounded-lg transition-colors {$page.route.id?.includes('policies') ? 'bg-primary/10 text-primary' : 'text-slate-600 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-900'}">Policies</a>
          </nav>
        {/if}
      </div>

      <div class="flex items-center gap-4">
        {#if $isAuthenticated}
          <div class="hidden sm:flex flex-col items-end mr-2">
            <span class="text-[10px] uppercase tracking-wider font-bold text-slate-400 leading-none mb-1">Operator</span>
            <span class="text-sm font-bold text-slate-900 dark:text-white">{$user?.username || 'User'}</span>
          </div>
          <button 
            on:click={logout}
            class="h-10 px-5 rounded-xl text-sm font-bold bg-slate-900 text-white dark:bg-white dark:text-slate-900 hover:opacity-90 transition-all active:scale-95 shadow-lg shadow-slate-200 dark:shadow-none"
          >
            Logout
          </button>
        {:else}
          <div class="flex items-center gap-2">
            <a 
              href="/login" 
              class="h-10 px-5 flex items-center rounded-xl text-sm font-bold text-slate-600 hover:text-slate-900 hover:bg-slate-100 dark:text-slate-400 dark:hover:text-white transition-all"
            >
              Log in
            </a>
            <a 
              href="/signup" 
              class="h-10 px-5 flex items-center rounded-xl text-sm font-black bg-primary text-white hover:opacity-90 transition-all active:scale-95 shadow-xl shadow-primary/20"
            >
              Get Started
            </a>
          </div>
        {/if}
      </div>
    </div>
  </header>

  <main class="flex-1 overflow-auto">
    <slot />
  </main>

  <footer class="border-t bg-white dark:bg-slate-950 py-8 px-6">
    <div class="container mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
      <div class="flex items-center gap-2">
        <span class="font-black text-lg tracking-tighter text-slate-900 dark:text-white uppercase">k-secure</span>
        <span class="text-slate-400 text-xs font-medium">© 2026 SecureCluster Inc.</span>
      </div>
      <div class="flex items-center gap-6 text-xs font-bold text-slate-500 dark:text-slate-400">
        <a href="/" class="hover:text-primary transition-colors">Privacy Policy</a>
        <a href="/" class="hover:text-primary transition-colors">Terms of Service</a>
        <a href="/" class="hover:text-primary transition-colors">Documentation</a>
      </div>
    </div>
  </footer>
</div>

<style>
  :global(body) {
    margin: 0;
    font-family: 'Inter', system-ui, -apple-system, sans-serif;
  }
  :global(::selection) {
    background-color: hsl(var(--primary) / 0.2);
    color: hsl(var(--primary));
  }
</style>
