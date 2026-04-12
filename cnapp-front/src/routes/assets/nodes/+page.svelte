<script lang="ts">
  import ResourcePage from '$lib/components/ResourcePage.svelte';
  import { formatDate } from '$lib/utils';
  import type { NodeDto } from '$lib/types';

  const columns = ['Name', 'OS Image', 'Kernel Version', 'Kubelet Version', 'Created At'];
</script>

<svelte:head>
  <title>Nodes | k-secure</title>
</svelte:head>

<ResourcePage 
  title="Nodes"
  description="Physical and virtual machines in your cluster."
  endpoint="/assets/nodes"
  {columns}
>
  {#snippet renderRow(node: NodeDto)}
    <tr class="transition-colors hover:bg-slate-50/50 dark:hover:bg-slate-900/30 group">
      <td class="px-6 py-4 align-middle">
        <span class="font-bold text-slate-900 dark:text-white leading-none mb-1 group-hover:text-primary transition-colors">{node.name}</span>
      </td>
      <td class="px-6 py-4 align-middle">
        <span class="text-xs font-semibold text-slate-600 dark:text-slate-400">{node.osImage}</span>
      </td>
      <td class="px-6 py-4 align-middle">
        <span class="text-xs font-mono text-slate-500">{node.kernelVersion}</span>
      </td>
      <td class="px-6 py-4 align-middle">
        <span class="inline-flex items-center px-2 py-0.5 rounded bg-blue-50 dark:bg-blue-900/20 text-[10px] font-bold text-blue-600 dark:text-blue-400 border border-blue-100 dark:border-blue-800">
          {node.kubeletVersion}
        </span>
      </td>
      <td class="px-6 py-4 align-middle tabular-nums whitespace-nowrap text-xs text-slate-500 font-medium">
        {formatDate(node.createdAt)}
      </td>
    </tr>
  {/snippet}
</ResourcePage>
