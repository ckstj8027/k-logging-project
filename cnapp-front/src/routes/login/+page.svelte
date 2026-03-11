<script lang="ts">
  import { goto } from '$app/navigation';
  import api from '$lib/api';
  import { setAuthToken } from '$lib/auth';

  let username = '';
  let password = '';
  let error: string | null = null;
  let loading = false;

  async function login() {
    loading = true;
    error = null;
    try {
      const response = await api.post('/login', { username, password });
      if (response.data.token) {
        setAuthToken(response.data.token, username);
        goto('/');
      }
    } catch (err: any) {
      if (err.response && err.response.data && typeof err.response.data === 'string') {
        error = err.response.data;
      } else {
        error = '로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.';
      }
      console.error(err);
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Login | k-secure</title>
</svelte:head>

<div class="flex flex-col items-center justify-center min-h-[calc(100vh-12rem)] px-4">
  <div class="w-full max-w-md p-8 space-y-8 bg-white dark:bg-slate-950 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-2xl">
    <div class="space-y-3 text-center">
      <div class="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-primary/10 mb-2">
        <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="text-primary"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
      </div>
      <h1 class="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">Welcome back</h1>
      <p class="text-slate-500 dark:text-slate-400 text-sm font-medium">Enter your credentials to access your dashboard.</p>
    </div>

    <form on:submit|preventDefault={login} class="space-y-5">
      <div class="space-y-2">
        <label for="username" class="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">Username</label>
        <input 
          id="username" 
          type="text" 
          bind:value={username} 
          required 
          disabled={loading}
          placeholder="admin"
          class="flex h-12 w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 px-4 py-2 text-sm ring-offset-white transition-all placeholder:text-slate-400 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
        />
      </div>

      <div class="space-y-2">
        <div class="flex items-center justify-between ml-1">
          <label for="password" class="text-sm font-bold text-slate-700 dark:text-slate-300">Password</label>
          <a href="/forgot-password" class="text-xs font-semibold text-primary hover:underline">Forgot password?</a>
        </div>
        <input 
          id="password" 
          type="password" 
          bind:value={password} 
          required 
          disabled={loading}
          placeholder="••••••••"
          class="flex h-12 w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 px-4 py-2 text-sm ring-offset-white transition-all placeholder:text-slate-400 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
        />
      </div>

      {#if error}
        <div class="text-sm font-semibold text-destructive bg-destructive/10 p-4 rounded-xl flex items-center gap-2 animate-in fade-in zoom-in-95">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {error}
        </div>
      {/if}

      <button 
        type="submit" 
        disabled={loading}
        class="inline-flex items-center justify-center rounded-xl text-sm font-bold ring-offset-white transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-slate-900 text-white dark:bg-white dark:text-slate-900 hover:opacity-90 h-12 px-6 py-2 w-full active:scale-[0.98] shadow-xl"
      >
        {#if loading}
          <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-current mr-2"></div>
        {/if}
        Sign in
      </button>
    </form>

    <div class="text-center text-sm">
      <p class="text-slate-500 dark:text-slate-400 font-medium">
        Don't have an account? 
        <a href="/signup" class="text-primary hover:underline font-bold ml-1">Create one now</a>
      </p>
    </div>
  </div>
</div>
