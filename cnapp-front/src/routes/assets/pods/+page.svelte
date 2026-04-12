<script lang="ts">
  import ResourcePage from '$lib/components/ResourcePage.svelte';
  import { formatDate } from '$lib/utils';
  import type { PodDto } from '$lib/types';

  const columns = [
    'Pod Name / Namespace',
    'Status',
    'Security (Priv/Root)',
    'Network & Node',
    'Parent / Image',
    'Collected At'
  ];

  const statusStyles: Record<string, string> = {
    Running: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400',
    Pending: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
    CrashLoopBackOff: 'bg-rose-100 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400',
    Failed: 'bg-rose-100 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400',
    Succeeded: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
  };
</script>

<svelte:head>
  <title>Pods | k-secure</title>
</svelte:head>

<ResourcePage 
  title="Pods"
  description="Detailed cluster pod inventory with real-time status and security configurations."
  endpoint="/assets/pods"
  {columns}
>
  {#snippet renderRow(pod: PodDto)}
    <tr class="transition-colors hover:bg-slate-50/50 dark:hover:bg-slate-900/30 group">
      <td class="px-6 py-4 align-middle">
        <div class="flex flex-col">
          <span class="font-bold text-slate-900 dark:text-white leading-none mb-1 group-hover:text-primary transition-colors">{pod.podName}</span>
          <div class="flex items-center gap-1">
            <span class="px-1.5 py-0.5 rounded bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-[10px] font-bold text-slate-400 uppercase">{pod.namespace}</span>
            <span class="text-[10px] font-bold text-slate-400 uppercase">{pod.containerName}</span>
          </div>
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
            <span class="text-xs font-bold text-slate-700 dark:text-slate-300">{pod.nodeName}</span>
          </div>
          <span class="text-[10px] font-mono font-bold text-slate-500">{pod.podIp || 'No IP'}</span>
        </div>
      </td>
      <td class="px-6 py-4 align-middle">
        <div class="flex flex-col space-y-1">
          <span class="text-xs font-bold text-slate-700 dark:text-slate-300 truncate max-w-[120px]">{pod.deploymentName || 'None'}</span>
          <code class="text-[10px] font-mono text-slate-500 truncate max-w-[150px]">{pod.image.split('/').pop()}</code>
        </div>
      </td>
      <td class="px-6 py-4 align-middle tabular-nums whitespace-nowrap text-right text-xs text-slate-500 font-medium">
        {formatDate(pod.createdAt)}
      </td>
    </tr>
  {/snippet}
</ResourcePage>
