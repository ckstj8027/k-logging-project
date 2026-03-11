<script lang="ts">
  import { goto } from '$app/navigation';
  import api from '$lib/api';

  let companyName = '';
  let username = '';
  let password = '';
  let apiKey: string | null = null;
  let error: string | null = null;
  let loading = false;
  let copied = false;

  async function signup() {
    loading = true;
    error = null;
    try {
      const response = await api.post('/signup', { 
        companyName, 
        username, 
        password 
      });
      
      // 서버로부터 apiKey를 성공적으로 받았을 때
      if (response.data && response.data.apiKey) {
        apiKey = response.data.apiKey;
      } else {
        alert('회원가입이 완료되었습니다. 로그인해주세요.');
        goto('/login');
      }
    } catch (err: any) {
      error = err.response?.data || '회원가입에 실패했습니다. 입력 정보를 확인해주세요.';
      console.error(err);
    } finally {
      loading = false;
    }
  }

  function copyToClipboard() {
    if (apiKey) {
      navigator.clipboard.writeText(apiKey);
      copied = true;
      setTimeout(() => (copied = false), 2000);
    }
  }
</script>

<svelte:head>
  <title>Sign Up | k-secure</title>
</svelte:head>

<div class="flex flex-col items-center justify-center min-h-[calc(100vh-12rem)] px-4">
  {#if !apiKey}
    <!-- 회원가입 폼 -->
    <div class="w-full max-w-md p-8 space-y-6 bg-white dark:bg-slate-950 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-2xl animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div class="space-y-3 text-center">
        <div class="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-primary/10 mb-2">
          <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="text-primary"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>
        </div>
        <h1 class="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">Create Account</h1>
        <p class="text-slate-500 dark:text-slate-400 text-sm font-medium">Join k-secure to manage your cluster security.</p>
      </div>

      <form on:submit|preventDefault={signup} class="space-y-5">
        <div class="space-y-2">
          <label for="company" class="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">Company Name</label>
          <input 
            id="company" 
            type="text" 
            bind:value={companyName} 
            required 
            disabled={loading}
            placeholder="e.g. MyCompany"
            class="flex h-12 w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 px-4 py-2 text-sm ring-offset-white transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:opacity-50"
          />
        </div>

        <div class="space-y-2">
          <label for="username" class="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">Admin Username</label>
          <input 
            id="username" 
            type="text" 
            bind:value={username} 
            required 
            disabled={loading}
            placeholder="admin"
            class="flex h-12 w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 px-4 py-2 text-sm ring-offset-white transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:opacity-50"
          />
        </div>

        <div class="space-y-2">
          <label for="password" class="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">Password</label>
          <input 
            id="password" 
            type="password" 
            bind:value={password} 
            required 
            disabled={loading}
            placeholder="••••••••"
            class="flex h-12 w-full rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 px-4 py-2 text-sm ring-offset-white transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:opacity-50"
          />
        </div>

        {#if error}
          <div class="text-sm font-semibold text-destructive bg-destructive/10 p-4 rounded-xl flex items-center gap-2 animate-in fade-in zoom-in-95">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            {error}
          </div>
        {/if}

        <button 
          type="submit" 
          disabled={loading}
          class="inline-flex items-center justify-center rounded-xl text-sm font-bold ring-offset-white transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 bg-slate-900 text-white dark:bg-white dark:text-slate-900 hover:opacity-90 h-12 px-6 py-2 w-full active:scale-[0.98] shadow-xl"
        >
          {#if loading}
            <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-current mr-2"></div>
          {/if}
          Create Admin Account
        </button>
      </form>

      <div class="text-center text-sm pt-2">
        <p class="text-slate-500 dark:text-slate-400 font-medium">
          Already registered? 
          <a href="/login" class="text-primary hover:underline font-bold ml-1 transition-colors">Go to Login</a>
        </p>
      </div>
    </div>
  {:else}
    <!-- 회원가입 성공 및 API Key 표시 화면 -->
    <div class="w-full max-w-xl p-10 space-y-8 bg-white dark:bg-slate-950 rounded-3xl border border-emerald-200 dark:border-emerald-900/50 shadow-2xl animate-in zoom-in-95 duration-500">
      <div class="space-y-4 text-center">
        <div class="inline-flex items-center justify-center w-20 h-20 rounded-full bg-emerald-100 dark:bg-emerald-900/30 mb-2">
          <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" class="text-emerald-600 dark:text-emerald-400"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        </div>
        <h1 class="text-4xl font-black tracking-tight text-slate-900 dark:text-white leading-tight">Registration Complete!</h1>
        <p class="text-slate-500 dark:text-slate-400 font-medium max-w-md mx-auto">
          Your account has been successfully created. Please save the API Key below for agent configuration.
        </p>
      </div>

      <div class="space-y-3">
        <div class="flex items-center justify-between px-1">
          <label for="apiKey" class="text-xs font-black uppercase tracking-widest text-slate-400">Agent API Key</label>
          <span class="text-[10px] font-bold text-rose-500 uppercase tracking-tighter">Save this securely!</span>
        </div>
        <div class="relative group">
          <div class="absolute -inset-1 bg-gradient-to-r from-emerald-500 to-teal-500 rounded-2xl blur opacity-25 group-hover:opacity-50 transition duration-1000 group-hover:duration-200"></div>
          <div class="relative flex items-center gap-3 p-1 rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
            <input 
              id="apiKey"
              type="text" 
              readonly 
              value={apiKey} 
              class="flex-1 bg-transparent border-none px-4 py-3 font-mono text-sm font-bold text-emerald-700 dark:text-emerald-400 focus:ring-0"
            />
            <button 
              on:click={copyToClipboard}
              class="inline-flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl text-xs font-bold transition-all {copied ? 'bg-emerald-500 text-white' : 'bg-white dark:bg-slate-800 text-slate-900 dark:text-white hover:bg-slate-100'} shadow-sm border border-slate-200 dark:border-slate-700 active:scale-95"
            >
              {#if copied}
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                Copied
              {:else}
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><rect width="14" height="14" x="8" y="8" rx="2" ry="2"/><path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"/></svg>
                Copy Key
              {/if}
            </button>
          </div>
        </div>
      </div>

      <div class="pt-4 flex flex-col gap-3">
        <button 
          on:click={() => goto('/login')}
          class="inline-flex items-center justify-center rounded-2xl text-sm font-black bg-slate-900 text-white dark:bg-white dark:text-slate-900 hover:opacity-90 h-14 px-8 py-2 w-full shadow-2xl transition-all hover:shadow-emerald-500/20 active:scale-[0.98]"
        >
          Go to Login Screen
        </button>
        <p class="text-center text-[10px] font-bold text-slate-400 uppercase tracking-widest">
          You can view your API Key again in the Dashboard after logging in.
        </p>
      </div>
    </div>
  {/if}
</div>
