<script lang="ts">
  import { onMount } from 'svelte';
  import api from '$lib/api';
  import { dashboardSummary } from '$lib/store';

  // Props definition
  let { 
    title, 
    description, 
    endpoint, 
    columns, 
    renderRow 
  } = $props();

  let items: any[] = $state([]);
  let error: string | null = $state(null);
  let loading = $state(false);
  let hasMore = $state(true);
  const pageSize = 20;

  // Map title to summary count keys
  const countKeyMap: any = {
    'Pods': 'podCount',
    'Nodes': 'nodeCount',
    'Deployments': 'deploymentCount',
    'Services': 'serviceCount',
    'Namespaces': 'namespaceCount',
    'Cluster Events': 'eventCount'
  };

  async function fetchItems(lastId?: number) {
    if (loading) return;
    loading = true;
    try {
      const response = await api.get(endpoint, {
        params: {
          lastId,
          size: pageSize
        }
      });
      
      const newItems = response.data;
      if (newItems.length < pageSize) {
        hasMore = false;
      }
      
      if (lastId) {
        items = [...items, ...newItems];
      } else {
        items = newItems;
      }
    } catch (err) {
      error = `${title} 정보를 불러오는 데 실패했습니다.`;
      console.error(err);
    } finally {
      loading = false;
    }
  }

  onMount(() => {
    fetchItems();
  });

  function loadMore() {
    if (items.length > 0) {
      const lastId = items[items.length - 1].id;
      fetchItems(lastId);
    }
  }

  // Reactive total count from global store
  let totalCount = $derived($dashboardSummary ? ($dashboardSummary as any)[countKeyMap[title]] : 0);
</script>

<div class="space-y-8 animate-in fade-in duration-500">
  <div class="flex items-center justify-between">
    <div class="space-y-1">
      <h1 class="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white uppercase tracking-tighter">{title}</h1>
      <p class="text-slate-500 dark:text-slate-400 font-medium">{description}</p>
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

  {#if items.length > 0}
    <div class="space-y-6">
      <div class="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950 shadow-xl shadow-slate-200/50 dark:shadow-none overflow-hidden transition-all">
        <div class="relative w-full overflow-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="bg-slate-50/80 dark:bg-slate-900/50 border-b border-slate-200 dark:border-slate-800">
                {#each columns as column}
                  <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">{column}</th>
                {/each}
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 dark:divide-slate-800">
              {#each items as item (item.id)}
                {@render renderRow(item)}
              {/each}
            </tbody>
          </table>
        </div>
      </div>

      <!-- Progress bar and Load more -->
      <div class="space-y-4">
        {#if totalCount > 0}
          <div class="flex flex-col items-center gap-2">
            <div class="flex justify-between w-full max-w-xs text-[10px] font-black uppercase tracking-widest text-slate-400">
              <span>Displaying</span>
              <span>{items.length} / {totalCount} items</span>
            </div>
            <div class="w-full max-w-xs h-1.5 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
              <div 
                class="h-full bg-primary transition-all duration-500 ease-out" 
                style="width: {(items.length / totalCount) * 100}%"
              ></div>
            </div>
          </div>
        {/if}

        {#if hasMore}
          <div class="flex justify-center">
            <button 
              onclick={loadMore}
              disabled={loading}
              class="inline-flex items-center justify-center rounded-xl text-sm font-bold ring-offset-white transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 bg-white dark:bg-slate-900 text-slate-900 dark:text-white border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 h-12 px-8 active:scale-[0.98] shadow-lg"
            >
              {#if loading}
                <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-primary mr-2"></div>
                Loading...
              {:else}
                Load More {title}
              {/if}
            </button>
          </div>
        {/if}
      </div>
    </div>
  {:else if !loading}
    <div class="flex flex-col items-center justify-center py-20 bg-slate-50/50 dark:bg-slate-900/20 rounded-3xl border border-dashed border-slate-200 dark:border-slate-800 animate-in fade-in zoom-in-95">
      <div class="w-16 h-16 bg-slate-100 dark:bg-slate-800 rounded-full flex items-center justify-center mb-4">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="text-slate-400"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
      </div>
      <p class="text-slate-500 dark:text-slate-400 font-bold">No {title} found</p>
      <p class="text-slate-400 dark:text-slate-500 text-sm">There are no active {title.toLowerCase()} in this cluster.</p>
    </div>
  {/if}
</div>
