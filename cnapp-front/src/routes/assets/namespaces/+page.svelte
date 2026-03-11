<script lang="ts">
  import { onMount } from 'svelte';
  import { formatDate } from '$lib/utils';
  import api from '$lib/api';
  import type { NamespaceDto, DashboardSummary } from '$lib/types';

  let namespaces: NamespaceDto[] = [];
  let summary: DashboardSummary | null = null;
  let error: string | null = null;
  let loading = false;
  let hasMore = true;
  const pageSize = 20;

  async function fetchNamespaces(lastId?: number) {
    if (loading) return;
    loading = true;
    try {
      const response = await api.get('/assets/namespaces', {
        params: {
          lastId,
          size: pageSize
        }
      });
      
      const newNamespaces = response.data;
      if (newNamespaces.length < pageSize) {
        hasMore = false;
      }
      
      if (lastId) {
        namespaces = [...namespaces, ...newNamespaces];
      } else {
        namespaces = newNamespaces;
      }
    } catch (err) {
      error = 'Failed to fetch namespaces.';
      console.error(err);
    } finally {
      loading = false;
    }
  }

  onMount(async () => {
    fetchNamespaces();
    try {
      const res = await api.get('/dashboard');
      summary = res.data;
    } catch (e) {
      console.error('Failed to fetch summary');
    }
  });

  function loadMore() {
    if (namespaces.length > 0) {
      const lastId = namespaces[namespaces.length - 1].id;
      fetchNamespaces(lastId);
    }
  }
</script>

<svelte:head>
  <title>Namespaces | k-secure</title>
</svelte:head>

<div class="space-y-8 animate-in fade-in duration-500">
  <div class="flex items-center justify-between">
    <div class="space-y-1">
      <h1 class="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white uppercase tracking-tighter">Namespaces</h1>
      <p class="text-slate-500 dark:text-slate-400 font-medium">Logical isolation and organization of cluster resources.</p>
    </div>
  </div>

  {#if error}
    <div class="rounded-2xl border border-destructive/20 bg-destructive/5 p-4 text-destructive flex items-center gap-3">
      <div class="bg-destructive/10 p-2 rounded-lg">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      </div>
      <span class="font-bold">{error}</span>
    </div>
  {/if}

  {#if namespaces.length > 0}
    <div class="space-y-6">
      <div class="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950 shadow-xl shadow-slate-200/50 dark:shadow-none overflow-hidden transition-all">
        <div class="relative w-full overflow-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="bg-slate-50/80 dark:bg-slate-900/50 border-b border-slate-200 dark:border-slate-800">
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Name</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Status</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Created At</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 dark:divide-slate-800">
              {#each namespaces as ns (ns.id)}
                <tr class="transition-colors hover:bg-slate-50/50 dark:hover:bg-slate-900/30 group">
                  <td class="px-6 py-4 align-middle">
                    <span class="font-bold text-slate-900 dark:text-white leading-none mb-1 group-hover:text-primary transition-colors">{ns.name}</span>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-black uppercase {ns.status === 'Active' ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-slate-100 text-slate-500 dark:bg-slate-800 dark:text-slate-400'}">
                      <span class="h-1.5 w-1.5 rounded-full {ns.status === 'Active' ? 'bg-emerald-500' : 'bg-slate-400'}"></span>
                      {ns.status}
                    </span>
                  </td>
                  <td class="px-6 py-4 align-middle tabular-nums whitespace-nowrap">
                    <span class="text-slate-500 font-medium">{formatDate(ns.createdAt)}</span>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      </div>

      <!-- 상태바 & 로드 더보기 -->
      <div class="space-y-4">
        {#if summary}
          <div class="flex flex-col items-center gap-2">
            <div class="flex justify-between w-full max-w-xs text-[10px] font-black uppercase tracking-widest text-slate-400">
              <span>Progress</span>
              <span>{namespaces.length} / {summary.namespaceCount} items</span>
            </div>
            <div class="w-full max-w-xs h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div 
                class="h-full bg-primary transition-all duration-500 ease-out" 
                style="width: {(namespaces.length / summary.namespaceCount) * 100}%"
              ></div>
            </div>
          </div>
        {/if}

        {#if hasMore}
          <div class="flex justify-center">
            <button 
              on:click={loadMore}
              disabled={loading}
              class="inline-flex items-center justify-center px-8 py-3 rounded-xl text-sm font-bold bg-white dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-900 dark:text-white hover:bg-slate-50 dark:hover:bg-slate-900 transition-all active:scale-95 disabled:opacity-50 shadow-lg shadow-slate-200/50 dark:shadow-none"
            >
              {#if loading}
                <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-primary mr-2"></div>
                Loading...
              {:else}
                Load More Namespaces
              {/if}
            </button>
          </div>
        {/if}
      </div>
    </div>
  {:else if !error && !loading}
    <div class="flex flex-col items-center justify-center h-64 border-2 border-dashed rounded-2xl bg-slate-50/50 dark:bg-slate-900/30 border-slate-200 dark:border-slate-800">
      <p class="text-sm text-slate-400 font-bold uppercase tracking-widest">No namespaces found.</p>
    </div>
  {:else if loading && namespaces.length === 0}
    <div class="flex flex-col items-center justify-center h-64 border-2 border-dashed rounded-2xl bg-slate-50/50 dark:bg-slate-900/30 border-slate-200 dark:border-slate-800 animate-pulse">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mb-2"></div>
      <p class="text-sm text-slate-400 font-bold uppercase tracking-widest">Loading namespaces...</p>
    </div>
  {/if}
</div>
