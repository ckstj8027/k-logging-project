<script lang="ts">
  import ResourcePage from '$lib/components/ResourcePage.svelte';
  import { formatDate } from '$lib/utils';
  import type { EventDto } from '$lib/types';

  const columns = ['Type', 'Reason', 'Object', 'Message', 'Namespace', 'Last Timestamp'];

  const typeStyles: Record<string, string> = {
    Normal: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400',
    Warning: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
    Error: 'bg-rose-100 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400'
  };
</script>

<svelte:head>
  <title>Events | k-secure</title>
</svelte:head>

<ResourcePage 
  title="Cluster Events"
  description="History of activities and status changes in your cluster."
  endpoint="/assets/events"
  {columns}
>
  {#snippet renderRow(event: EventDto)}
    <tr class="transition-colors hover:bg-slate-50/50 dark:hover:bg-slate-900/30 group">
      <td class="px-6 py-4 align-middle">
        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-black uppercase {typeStyles[event.type] || 'bg-slate-100 text-slate-500'}">
          <span class="h-1.5 w-1.5 rounded-full {event.type === 'Normal' ? 'bg-emerald-500' : (event.type === 'Warning' ? 'bg-amber-500' : 'bg-rose-500')}"></span>
          {event.type}
        </span>
      </td>
      <td class="px-6 py-4 align-middle font-bold text-slate-900 dark:text-white">{event.reason}</td>
      <td class="px-6 py-4 align-middle">
        <div class="flex flex-col">
          <span class="text-[10px] uppercase font-bold text-slate-400 mb-0.5">{event.involvedObjectKind}</span>
          <span class="font-mono text-xs font-bold text-slate-700 dark:text-slate-300">{event.involvedObjectName}</span>
        </div>
      </td>
      <td class="px-6 py-4 align-middle">
        <p class="text-xs text-slate-600 dark:text-slate-400 line-clamp-2 max-w-md">{event.message}</p>
      </td>
      <td class="px-6 py-4 align-middle">
        <span class="inline-flex items-center rounded-lg border border-slate-200 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 px-2.5 py-1 text-xs font-bold text-slate-700 dark:text-slate-300">
          {event.namespace}
        </span>
      </td>
      <td class="px-6 py-4 align-middle tabular-nums whitespace-nowrap text-xs text-slate-500 font-medium text-right">
        {formatDate(event.lastTimestamp)}
      </td>
    </tr>
  {/snippet}
</ResourcePage>
