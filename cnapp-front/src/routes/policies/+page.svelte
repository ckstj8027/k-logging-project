<script lang="ts">
  import { onMount } from 'svelte';
  import api from '$lib/api';
  import type { PolicyDto, DashboardSummary } from '$lib/types';

  let policies: PolicyDto[] = [];
  let summary: DashboardSummary | null = null;
  let error: string | null = null;
  let loading = false;
  let hasMore = true;
  const pageSize = 20;

  let editingId: number | null = null;
  let editValue: string = '';
  let expandedValueIds = new Set<number>();

  async function fetchPolicies(lastId?: number) {
    if (loading) return;
    loading = true;
    try {
      const response = await api.get('/policies', {
        params: {
          lastId,
          size: pageSize
        }
      });
      
      const newPolicies = response.data;
      if (newPolicies.length < pageSize) {
        hasMore = false;
      }
      
      if (lastId) {
        policies = [...policies, ...newPolicies];
      } else {
        policies = newPolicies;
      }
    } catch (err) {
      error = 'Failed to fetch policies.';
      console.error(err);
    } finally {
      loading = false;
    }
  }

  onMount(async () => {
    fetchPolicies();
    try {
      const res = await api.get('/dashboard');
      summary = res.data;
    } catch (e) {
      console.error('Failed to fetch summary');
    }
  });

  function loadMore() {
    if (policies.length > 0) {
      const lastId = policies[policies.length - 1].id;
      fetchPolicies(lastId);
    }
  }

  function toggleValue(id: number) {
    if (expandedValueIds.has(id)) {
      expandedValueIds.delete(id);
    } else {
      expandedValueIds.add(id);
    }
    expandedValueIds = new Set(expandedValueIds);
  }

  function formatValue(value: string, id: number) {
    if (expandedValueIds.has(id) || value.length <= 10) {
      return value;
    }
    return value.slice(0, 10) + '...';
  }

  function startEdit(policy: PolicyDto) {
    editingId = policy.id;
    editValue = policy.value;
  }

  function cancelEdit() {
    editingId = null;
    editValue = '';
  }

  async function savePolicy(policy: PolicyDto) {
    try {
      await api.put(`/policies/${policy.id}`, { 
        value: editValue, 
        enabled: policy.enabled 
      });
      policies = policies.map(p => p.id === policy.id ? { ...p, value: editValue } : p);
      editingId = null;
    } catch (err) {
      console.error('Failed to update policy value', err);
      alert('정책 업데이트 실패');
    }
  }

  async function togglePolicy(policy: PolicyDto) {
    try {
      const updatedEnabled = !policy.enabled;
      await api.put(`/policies/${policy.id}`, { 
        value: policy.value, 
        enabled: updatedEnabled 
      });
      policies = policies.map(p => p.id === policy.id ? { ...p, enabled: updatedEnabled } : p);
    } catch (err) {
      console.error('Failed to update policy status', err);
      alert('정책 상태 업데이트 실패');
    }
  }
</script>

<svelte:head>
  <title>Policies | k-secure</title>
</svelte:head>

<div class="space-y-8 animate-in fade-in duration-500">
  <div class="flex items-center justify-between">
    <div class="space-y-1">
      <h1 class="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white uppercase tracking-tighter">Security Policies</h1>
      <p class="text-slate-500 dark:text-slate-400 font-medium">Configure and manage cluster security policies and rules.</p>
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

  {#if policies.length > 0}
    <div class="space-y-6">
      <div class="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950 shadow-xl shadow-slate-200/50 dark:shadow-none overflow-hidden transition-all">
        <div class="relative w-full overflow-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="bg-slate-50/80 dark:bg-slate-900/50 border-b border-slate-200 dark:border-slate-800">
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Resource</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Rule Type</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Description</th>
                <th class="h-14 px-6 text-left align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Status</th>
                <th class="h-14 px-6 text-right align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Value</th>
                <th class="h-14 px-6 text-right align-middle font-bold text-slate-500 uppercase tracking-widest text-[10px]">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 dark:divide-slate-800">
              {#each policies as policy (policy.id)}
                <tr class="transition-colors hover:bg-slate-50/50 dark:hover:bg-slate-900/30 group">
                  <td class="px-6 py-4 align-middle">
                    <span class="inline-flex items-center px-2 py-0.5 rounded bg-slate-100 dark:bg-slate-800 text-[10px] font-bold uppercase text-slate-600 dark:text-slate-400 border border-slate-200 dark:border-slate-700">
                      {policy.resourceType}
                    </span>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <span class="font-bold text-slate-900 dark:text-white">{policy.ruleType}</span>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <span class="text-slate-500 dark:text-slate-400 font-medium block max-w-xs">{policy.description}</span>
                  </td>
                  <td class="px-6 py-4 align-middle">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-black uppercase {policy.enabled ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400' : 'bg-slate-100 text-slate-500 dark:bg-slate-800 dark:text-slate-400'}">
                      <span class="h-1.5 w-1.5 rounded-full {policy.enabled ? 'bg-emerald-500' : 'bg-slate-400'}"></span>
                      {policy.enabled ? 'Enabled' : 'Disabled'}
                    </span>
                  </td>
                  <td class="px-6 py-4 align-middle text-right">
                    {#if editingId === policy.id}
                      <input 
                        type="text" 
                        bind:value={editValue} 
                        class="h-9 w-32 ml-auto rounded-lg border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 px-3 py-1 text-xs focus:outline-none focus:ring-2 focus:ring-primary"
                      />
                    {:else}
                      <button 
                        on:click={() => toggleValue(policy.id)}
                        class="text-xs font-mono bg-slate-50 dark:bg-slate-900 px-2 py-1 rounded text-primary font-bold hover:bg-primary/5 transition-colors"
                        title={policy.value}
                      >
                        {formatValue(policy.value, policy.id)}
                      </button>
                    {/if}
                  </td>
                  <td class="px-6 py-4 align-middle text-right">
                    <div class="flex items-center justify-end gap-2">
                      {#if editingId === policy.id}
                        <button on:click={() => savePolicy(policy)} class="h-8 px-3 rounded-lg text-xs font-bold bg-primary text-white hover:opacity-90 transition-all">Save</button>
                        <button on:click={cancelEdit} class="h-8 px-3 rounded-lg text-xs font-bold bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-200 transition-all">Cancel</button>
                      {:else}
                        <button on:click={() => startEdit(policy)} class="h-8 px-3 rounded-lg text-xs font-bold border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-900 transition-all">Edit</button>
                        <button 
                          on:click={() => togglePolicy(policy)}
                          class="h-8 px-3 rounded-lg text-xs font-bold transition-all {policy.enabled ? 'text-rose-500 hover:bg-rose-50' : 'text-primary hover:bg-primary/5'}"
                        >
                          {policy.enabled ? 'Disable' : 'Enable'}
                        </button>
                      {/if}
                    </div>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      </div>

      <!-- 상태바 & 로드 더보기 -->
      <div class="space-y-4 text-center">
        <div class="flex flex-col items-center gap-2">
          <div class="w-full max-w-xs h-1 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
            <div class="h-full bg-primary/30 animate-pulse w-full"></div>
          </div>
        </div>

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
                Load More Policies
              {/if}
            </button>
          </div>
        {/if}
      </div>
    </div>
  {:else if !error && !loading}
    <div class="flex flex-col items-center justify-center h-64 border-2 border-dashed rounded-2xl bg-slate-50/50 dark:bg-slate-900/30 border-slate-200 dark:border-slate-800">
      <p class="text-sm text-slate-400 font-bold uppercase tracking-widest">No policies found.</p>
    </div>
  {:else if loading && policies.length === 0}
    <div class="flex flex-col items-center justify-center h-64 border-2 border-dashed rounded-2xl bg-slate-50/50 dark:bg-slate-900/30 border-slate-200 dark:border-slate-800 animate-pulse">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mb-2"></div>
      <p class="text-sm text-slate-400 font-bold uppercase tracking-widest">Loading policies...</p>
    </div>
  {/if}
</div>
