<script lang="ts">
  import { onMount } from 'svelte';
  import api from '$lib/api';
  import type { AlertDto, DashboardSummary } from '$lib/types';
  import { formatDate } from '$lib/utils';

  let alerts: AlertDto[] = [];
  let summary: DashboardSummary | null = null;
  let error: string | null = null;
  let loading = false;
  let hasMore = true;
  const pageSize = 20;

  async function fetchAlerts(lastId?: number) {
    if (loading) return;
    loading = true;
    try {
      const response = await api.get('/alerts', {
        params: {
          lastId,
          size: pageSize
        }
      });
      
      const newAlerts = response.data;
      if (newAlerts.length < pageSize) {
        hasMore = false;
      }
      
      if (lastId) {
        alerts = [...alerts, ...newAlerts];
      } else {
        alerts = newAlerts;
      }
    } catch (err) {
      error = 'Failed to fetch alerts.';
      console.error(err);
    } finally {
      loading = false;
    }
  }

  onMount(async () => {
    fetchAlerts();
    try {
      const res = await api.get('/dashboard');
      summary = res.data;
    } catch (e) {
      console.error('Failed to fetch summary');
    }
  });

  function loadMore() {
    if (alerts.length > 0) {
      const lastId = alerts[alerts.length - 1].id;
      fetchAlerts(lastId);
    }
  }

  const severityStyles = {
    CRITICAL: 'bg-rose-500 text-white dark:bg-rose-600 shadow-rose-200 dark:shadow-none',
    HIGH: 'bg-orange-500 text-white dark:bg-orange-600 shadow-orange-200 dark:shadow-none',
    MEDIUM: 'bg-amber-400 text-amber-950 dark:bg-amber-500 dark:text-amber-950 shadow-amber-100 dark:shadow-none',
    LOW: 'bg-blue-400 text-white dark:bg-blue-500 shadow-blue-100 dark:shadow-none'
  };

  const categoryStyles = {
    CSPM: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300 border-slate-200 dark:border-slate-700',
    RUNTIME: 'bg-indigo-50 text-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-400 border-indigo-100 dark:border-indigo-800'
  };
</script>

<svelte:head>
  <title>Alerts | k-secure</title>
</svelte:head>

<div class="space-y-8 animate-in fade-in duration-500">
  <div class="flex items-center justify-between">
    <div class="space-y-1">
      <h1 class="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white">Security Alerts</h1>
      <p class="text-slate-500 dark:text-slate-400 font-medium">Monitor and manage security threats and compliance violations.</p>
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

  {#if alerts.length > 0}
    <div class="space-y-6">
      <div class="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950 shadow-xl shadow-slate-200/50 dark:shadow-none overflow-hidden transition-all">
        <div class="relative w-full overflow-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="bg-slate-50/80 dark:bg-slate-900/50 border-b border-slate-200 dark:border-slate-800">
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Severity</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Category</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Message</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Affected Resource</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Status</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Time Detected</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 dark:divide-slate-800">
              {#each alerts as alert (alert.id)}
                <tr class="transition-colors hover:bg-slate-50/50 dark:hover:bg-slate-900/30 group">
                  <td class="px-6 py-4 align-middle">
                    <span class="inline-flex items-center justify-center px-3 py-1 rounded-lg text-[10px] font-black uppercase tracking-widest shadow-lg {severityStyles[alert.severity]}">
                      {alert.severity}
                    </span>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <span class="inline-flex items-center px-2.5 py-1 rounded-md border text-[10px] font-bold uppercase tracking-tighter {categoryStyles[alert.category]}">
                      {alert.category}
                    </span>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <span class="font-bold text-slate-900 dark:text-white leading-tight block max-w-sm">{alert.message}</span>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <div class="flex flex-col">
                      <span class="text-[10px] uppercase font-bold text-slate-400 mb-1">{alert.resourceType}</span>
                      <span class="font-mono text-xs font-bold text-slate-700 dark:text-slate-300 group-hover:text-primary transition-colors">{alert.resourceName}</span>
                    </div>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-black uppercase {alert.status === 'OPEN' ? 'bg-rose-100 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400' : 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400'}">
                      <span class="h-1.5 w-1.5 rounded-full {alert.status === 'OPEN' ? 'bg-rose-500 animate-pulse' : 'bg-emerald-500'}"></span>
                      {alert.status}
                    </span>
                  </td>
                  <td class="px-6 py-4 align-middle tabular-nums whitespace-nowrap">
                    <div class="flex flex-col font-medium">
                      <span class="text-slate-700 dark:text-slate-300">{formatDate(alert.createdAt).split(' ')[0]}</span>
                      <span class="text-[10px] text-slate-400 font-bold tracking-tighter">{formatDate(alert.createdAt).split(' ')[1]}</span>
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
              <span>Progress</span>
              <span>{alerts.length} / {summary.alertCount} items</span>
            </div>
            <div class="w-full max-w-xs h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div 
                class="h-full bg-primary transition-all duration-500 ease-out" 
                style="width: {(alerts.length / summary.alertCount) * 100}%"
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
                Load More Alerts
              {/if}
            </button>
          </div>
        {/if}
      </div>
    </div>
  {:else if !error && !loading}
    <div class="flex flex-col items-center justify-center h-64 border-2 border-dashed rounded-2xl bg-slate-50/50 dark:bg-slate-900/30 border-slate-200 dark:border-slate-800">
      <p class="text-sm text-slate-400 font-bold uppercase tracking-widest">No alerts found.</p>
    </div>
  {:else if loading && alerts.length === 0}
    <div class="flex flex-col items-center justify-center h-64 border-2 border-dashed rounded-2xl bg-slate-50/50 dark:bg-slate-900/30 border-slate-200 dark:border-slate-800 animate-pulse">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mb-2"></div>
      <p class="text-sm text-slate-400 font-bold uppercase tracking-widest">Scanning for threats...</p>
    </div>
  {/if}
</div>
