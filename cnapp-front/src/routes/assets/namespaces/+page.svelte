<script lang="ts">
  import ResourcePage from '$lib/components/ResourcePage.svelte';
  import { formatDate } from '$lib/utils';
  import type { NamespaceDto } from '$lib/types';

  const columns = ['Name', 'Status', 'Created At'];
</script>

<svelte:head>
  <title>Namespaces | k-secure</title>
</svelte:head>

<ResourcePage 
  title="Namespaces"
  description="Logical isolation and organization of cluster resources."
  endpoint="/assets/namespaces"
  {columns}
>
  {#snippet renderRow(ns: NamespaceDto)}
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
      <td class="px-6 py-4 align-middle tabular-nums whitespace-nowrap text-xs text-slate-500 font-medium">
        {formatDate(ns.createdAt)}
      </td>
    </tr>
  {/snippet}
</ResourcePage>
