<script lang="ts">
  import ResourcePage from '$lib/components/ResourcePage.svelte';
  import { formatDate } from '$lib/utils';
  import type { DeploymentDto } from '$lib/types';

  const columns = ['Name', 'Namespace', 'Replicas (Avail/Total)', 'Strategy', 'Created At'];
</script>

<svelte:head>
  <title>Deployments | k-secure</title>
</svelte:head>

<ResourcePage 
  title="Deployments"
  description="Manage and monitor cluster application deployments."
  endpoint="/assets/deployments"
  {columns}
>
  {#snippet renderRow(dep: DeploymentDto)}
    <tr class="transition-colors hover:bg-slate-50/50 dark:hover:bg-slate-900/30 group">
      <td class="px-6 py-4 align-middle">
        <span class="font-bold text-slate-900 dark:text-white leading-none mb-1 group-hover:text-primary transition-colors">{dep.name}</span>
      </td>
      <td class="px-6 py-4 align-middle">
        <span class="inline-flex items-center rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 px-2.5 py-1 text-xs font-bold text-slate-700 dark:text-slate-300">
          {dep.namespace}
        </span>
      </td>
      <td class="px-6 py-4 align-middle">
        <div class="flex items-center gap-2">
          <div class="h-2 w-24 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
            <div 
              class="h-full bg-primary" 
              style="width: {(dep.availableReplicas / dep.replicas) * 100}%"
            ></div>
          </div>
          <span class="text-xs font-bold text-slate-700 dark:text-slate-300">
            {dep.availableReplicas}/{dep.replicas}
          </span>
        </div>
      </td>
      <td class="px-6 py-4 align-middle">
        <span class="text-xs font-semibold text-slate-600 dark:text-slate-400">{dep.strategyType}</span>
      </td>
      <td class="px-6 py-4 align-middle tabular-nums whitespace-nowrap text-xs text-slate-500 font-medium">
        {formatDate(dep.createdAt)}
      </td>
    </tr>
  {/snippet}
</ResourcePage>
