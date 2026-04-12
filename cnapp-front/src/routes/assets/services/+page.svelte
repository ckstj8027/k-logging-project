<script lang="ts">
  import ResourcePage from '$lib/components/ResourcePage.svelte';
  import { formatDate } from '$lib/utils';
  import type { ServiceDto } from '$lib/types';

  const columns = ['Name', 'Namespace', 'Type', 'Cluster IP', 'External IPs', 'Created At'];
</script>

<svelte:head>
  <title>Services | k-secure</title>
</svelte:head>

<ResourcePage 
  title="Services"
  description="Network services and cluster communication endpoints."
  endpoint="/assets/services"
  {columns}
>
  {#snippet renderRow(svc: ServiceDto)}
    <tr class="transition-colors hover:bg-slate-50/50 dark:hover:bg-slate-900/30 group">
      <td class="px-6 py-4 align-middle">
        <span class="font-bold text-slate-900 dark:text-white leading-none mb-1 group-hover:text-primary transition-colors">{svc.name}</span>
      </td>
      <td class="px-6 py-4 align-middle">
        <span class="inline-flex items-center rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 px-2.5 py-1 text-xs font-bold text-slate-700 dark:text-slate-300">
          {svc.namespace}
        </span>
      </td>
      <td class="px-6 py-4 align-middle">
        <span class="inline-flex items-center rounded-md px-2 py-0.5 text-[10px] font-black uppercase tracking-tighter bg-indigo-100 dark:bg-indigo-900/30 text-indigo-700 dark:text-indigo-400 border border-indigo-200 dark:border-indigo-800">
          {svc.type}
        </span>
      </td>
      <td class="px-6 py-4 align-middle font-mono text-xs text-slate-600 dark:text-slate-400">
        {svc.clusterIp}
      </td>
      <td class="px-6 py-4 align-middle">
        {#if svc.externalIps && svc.externalIps.length > 0}
          <div class="flex flex-wrap gap-1">
            {#each svc.externalIps as ip}
              <span class="text-[10px] font-mono text-slate-600 dark:text-slate-400">{ip}</span>
            {/each}
          </div>
        {:else}
          <span class="text-xs text-slate-400 italic">None</span>
        {/if}
      </td>
      <td class="px-6 py-4 align-middle tabular-nums whitespace-nowrap text-xs text-slate-500 font-medium">
        {formatDate(svc.createdAt)}
      </td>
    </tr>
  {/snippet}
</ResourcePage>
