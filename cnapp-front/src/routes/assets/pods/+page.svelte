<script lang="ts">
  import { onMount } from 'svelte';
  import api from '$lib/api';
  import type { PodDto, DashboardSummary } from '$lib/types';
  import { formatDate } from '$lib/utils';

  let pods: PodDto[] = [];
  let summary: DashboardSummary | null = null;
  let error: string | null = null;
  let loading = false;
  let hasMore = true;
  const pageSize = 20;

  async function fetchPods(lastId?: number) {
    if (loading) return;
    loading = true;
    try {
      const response = await api.get('/assets/pods', {
        params: {
          lastId,
          size: pageSize
        }
      });
      
      const newPods = response.data;
      if (newPods.length < pageSize) {
        hasMore = false;
      }
      
      if (lastId) {
        pods = [...pods, ...newPods];
      } else {
        pods = newPods;
      }
    } catch (err) {
      error = 'Failed to fetch pods.';
      console.error(err);
    } finally {
      loading = false;
    }
  }

  onMount(async () => {
    fetchPods();
    try {
      const res = await api.get('/dashboard');
      summary = res.data;
    } catch (e) {
      console.error('Failed to fetch summary');
    }
  });

  function loadMore() {
    if (pods.length > 0) {
      const lastId = pods[pods.length - 1].id;
      fetchPods(lastId);
    }
  }

  const statusStyles = {
    Running: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400',
    Pending: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
    CrashLoopBackOff: 'bg-rose-100 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400',
    Failed: 'bg-rose-100 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400',
    Succeeded: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
  };
</script>

<svelte:head>
  <title>Pod Assets | k-secure</title>
</svelte:head>

<div class="space-y-8 animate-in fade-in duration-500">
  <div class="flex items-center justify-between">
    <div class="space-y-1">
      <h1 class="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white uppercase tracking-tighter">Pods</h1>
      <p class="text-slate-500 dark:text-slate-400 font-medium">Detailed cluster pod inventory with real-time status and security configurations.</p>
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

  {#if pods.length > 0}
    <div class="space-y-6">
      <div class="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950 shadow-xl shadow-slate-200/50 dark:shadow-none overflow-hidden transition-all">
        <div class="relative w-full overflow-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="bg-slate-50/80 dark:bg-slate-900/50 border-b border-slate-200 dark:border-slate-800">
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Pod Name / Namespace</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Status</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Security (Priv/Root)</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Network & Node</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Parent / Image</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Collected At</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 dark:divide-slate-800">
              {#each pods as pod (pod.id)}
                <tr class="transition-colors hover:bg-slate-50/50 dark:hover:bg-slate-900/30 group">
                  <td class="px-6 py-4 align-middle">
                    <div class="flex flex-col">
                      <span class="font-bold text-slate-900 dark:text-white leading-none mb-1 group-hover:text-primary transition-colors">{pod.podName}</span>
                      <span class="inline-flex items-center text-[10px] font-bold text-slate-400 uppercase">
                        <span class="px-1.5 py-0.5 rounded bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 mr-1">{pod.namespace}</span>
                        {pod.containerName}
                      </span>
                    </div>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-black uppercase {statusStyles[pod.status] || 'bg-slate-100 text-slate-500'}">
                      <span class="h-1.5 w-1.5 rounded-full {pod.status === 'Running' ? 'bg-emerald-500 animate-pulse' : 'bg-slate-400'}"></span>
                      {pod.status}
                    </span>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <div class="flex gap-2">
                      <span class="inline-flex items-center justify-center h-8 px-2 rounded-lg border text-[9px] font-black uppercase transition-all {pod.privileged ? 'bg-rose-50 text-rose-600 border-rose-200 dark:bg-rose-950/30 dark:border-rose-800' : 'bg-slate-50 text-slate-400 border-slate-200 dark:bg-slate-900 dark:border-slate-800'}">
                        Privileged
                      </span>
                      <span class="inline-flex items-center justify-center h-8 px-2 rounded-lg border text-[9px] font-black uppercase transition-all {pod.runAsRoot ? 'bg-amber-50 text-amber-600 border-amber-200 dark:bg-amber-950/30 dark:border-amber-800' : 'bg-emerald-50 text-emerald-600 border-emerald-200 dark:bg-emerald-950/30 dark:border-emerald-800'}">
                        {pod.runAsRoot ? 'Root' : 'Non-Root'}
                      </span>
                    </div>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <div class="flex flex-col">
                      <div class="flex items-center gap-1.5 mb-1">
                        <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" class="text-slate-400"><rect x="2" y="2" width="20" height="8" rx="2"/><rect x="2" y="14" width="20" height="8" rx="2"/><line x1="6" y1="6" x2="6" y2="6"/><line x1="6" y1="18" x2="6" y2="18"/></svg>
                        <span class="text-xs font-bold text-slate-700 dark:text-slate-300">{pod.nodeName}</span>
                      </div>
                      <div class="flex items-center gap-1.5">
                        <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" class="text-slate-400"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
                        <span class="text-[10px] font-mono font-bold text-slate-500">{pod.podIp || 'No IP'}</span>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <div class="flex flex-col space-y-1">
                      <div class="flex items-center gap-1">
                        <span class="text-[9px] font-black text-primary uppercase">Parent</span>
                        <span class="text-xs font-bold text-slate-700 dark:text-slate-300 truncate max-w-[120px]" title={pod.deploymentName || 'N/A'}>
                          {pod.deploymentName || 'None'}
                        </span>
                      </div>
                      <div class="flex items-center gap-1">
                        <span class="text-[9px] font-black text-slate-400 uppercase">Image</span>
                        <code class="text-[10px] font-mono text-slate-500 bg-slate-50 dark:bg-slate-900 px-1.5 py-0.5 rounded border border-slate-100 dark:border-slate-800 truncate max-w-[150px]" title={pod.image}>
                          {pod.image.split('/').pop()}
                        </code>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-4 align-middle tabular-nums whitespace-nowrap text-right">
                    <div class="flex flex-col font-medium">
                      <span class="text-slate-700 dark:text-slate-300 text-xs">{formatDate(pod.createdAt).split(' ')[0]}</span>
                      <span class="text-[10px] text-slate-400 font-bold">{formatDate(pod.createdAt).split(' ')[1]}</span>
                    </div>
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
              <span>Displaying</span>
              <span>{pods.length} / {summary.podCount} Pods</span>
            </div>
            <div class="w-full max-w-xs h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div 
                class="h-full bg-primary transition-all duration-500 ease-out" 
                style="width: {(pods.length / summary.podCount) * 100}%"
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
                Load More Pods
              {/if}
            </button>
          </div>
        {/if}
      </div>
    </div>
  {:else if !error && !loading}
    <div class="flex flex-col items-center justify-center h-64 border-2 border-dashed rounded-2xl bg-slate-50/50 dark:bg-slate-900/30 border-slate-200 dark:border-slate-800">
      <p class="text-sm text-slate-400 font-bold uppercase tracking-widest">No pods found.</p>
    </div>
  {:else if loading && pods.length === 0}
    <div class="flex flex-col items-center justify-center h-64 border-2 border-dashed rounded-2xl bg-slate-50/50 dark:bg-slate-900/30 border-slate-200 dark:border-slate-800 animate-pulse">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mb-2"></div>
      <p class="text-sm text-slate-400 font-bold uppercase tracking-widest">Analyzing cluster assets...</p>
    </div>
  {/if}
</div>
