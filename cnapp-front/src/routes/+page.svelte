<script lang="ts">
  import { onMount, tick } from 'svelte';
  import api from '$lib/api';
  import { isAuthenticated } from '$lib/auth';
  import type { DashboardSummary } from '$lib/types';

  let summary: DashboardSummary | null = null;
  let error: string | null = null;

  // 스크롤 애니메이션 초기화 함수
  async function initObserver() {
    // DOM이 완전히 업데이트될 때까지 대기
    await tick();
    
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('reveal');
        }
      });
    }, { threshold: 0.1 });

    document.querySelectorAll('.animate-on-scroll').forEach(el => {
      // 기존 클래스 제거 후 다시 관찰 (안정성 확보)
      el.classList.remove('reveal');
      observer.observe(el);
    });
  }

  // 인증 상태가 변할 때마다 (로그아웃 포함) 애니메이션 초기화
  $: if (!$isAuthenticated) {
    initObserver();
  }

  onMount(async () => {
    if ($isAuthenticated) {
      try {
        const response = await api.get('/dashboard');
        summary = response.data;
      } catch (err) {
        error = 'Failed to fetch dashboard data.';
        console.error(err);
      }
    }
  });

  const cards = [
    { label: 'Pods', key: 'podCount', icon: 'M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10', color: 'bg-blue-500/10 text-blue-600 dark:text-blue-400' },
    { label: 'Services', key: 'serviceCount', icon: 'M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5', color: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400' },
    { label: 'Nodes', key: 'nodeCount', icon: 'M20 7h-9m3 4H5m12 4h-9M5 20h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z', color: 'bg-amber-500/10 text-amber-600 dark:text-amber-400' },
    { label: 'Namespaces', key: 'namespaceCount', icon: 'M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z', color: 'bg-purple-500/10 text-purple-600 dark:text-purple-400' },
    { label: 'Deployments', key: 'deploymentCount', icon: 'M9 17l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z', color: 'bg-indigo-500/10 text-indigo-600 dark:text-indigo-400' },
    { label: 'Events', key: 'eventCount', icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z', color: 'bg-slate-500/10 text-slate-600 dark:text-slate-400' },
    { label: 'Alerts', key: 'alertCount', icon: 'M12 22a2 2 0 002-2H10a2 2 0 002 2zm6-6V10a6 6 0 00-9-5.12V4a3 3 0 10-6 0v.88A6 6 0 003 10v6l-2 2v1h18v-1l-2-2z', color: 'bg-rose-500/10 text-rose-600 dark:text-rose-400' }
  ];

  const coreValues = [
    { title: 'Asset Inventory', desc: '클러스터 내 모든 자산을 실시간으로 시각화합니다.', icon: 'M20 7h-9m3 4H5m12 4h-9M5 20h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z' },
    { title: 'Real-time Tracking', desc: '이벤트를 추적하여 리소스 상태 변화를 감시합니다.', icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z' },
    { title: 'Security Policy', desc: '사용자 정의 정책으로 설정 오류를 탐지합니다.', icon: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z' }
  ];

  const discoveryAssets = [
    { type: 'Node', name: 'worker-01', icon: 'M20 7h-9m3 4H5m12 4h-9', color: 'text-amber-400' },
    { type: 'Pod', name: 'nginx-auth', icon: 'M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10', color: 'text-blue-400' },
    { type: 'Service', name: 'load-balancer', icon: 'M12 2L2 7l10 5 10-5-10-5z', color: 'text-emerald-400' },
    { type: 'Pod', name: 'db-redis', icon: 'M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10', color: 'text-blue-400' }
  ];

  const policyItems = [
    { label: 'Pod Profiles', rule: 'Image Tag Check: latest', value: 'Enabled', enabled: true },
    { label: 'Deployment Replicas', rule: 'Current Detection Limit', value: '3', enabled: true },
    { label: 'Node Threshold', rule: 'CPU/Memory Usage: 1.0', value: 'Default', enabled: false }
  ];
</script>

<svelte:head>
  <title>{$isAuthenticated ? 'Dashboard' : 'k-secure - Secure Your k8s Clusters'}</title>
</svelte:head>

{#if $isAuthenticated}
  <div class="space-y-8 animate-in fade-in duration-700 px-6">
    <div class="flex items-center justify-between">
      <div class="space-y-1">
        <h1 class="text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white uppercase tracking-tighter">Cluster Overview</h1>
        <p class="text-slate-500 dark:text-slate-400 font-medium">Real-time status of your Kubernetes environment.</p>
      </div>
      <div class="flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400 text-xs font-bold border border-emerald-200 dark:border-emerald-800 shadow-sm">
        <span class="relative flex h-2 w-2">
          <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
          <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
        </span>
        Connected
      </div>
    </div>

    {#if summary}
      <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        {#each cards as card}
          <div class="group relative overflow-hidden rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950 p-6 shadow-sm transition-all hover:shadow-xl hover:-translate-y-1">
            <div class="flex flex-row items-center justify-between space-y-0 pb-4">
              <h3 class="text-sm font-bold tracking-wider text-slate-500 dark:text-slate-400 uppercase">{card.label}</h3>
              <div class="p-2.5 rounded-xl transition-all group-hover:scale-110 {card.color}">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d={card.icon} />
                </svg>
              </div>
            </div>
            <div>
              <div class="text-3xl font-black text-slate-900 dark:text-white">{(summary as any)[card.key]}</div>
              <p class="text-[10px] font-bold text-slate-400 mt-1 uppercase tracking-widest">Active Monitoring</p>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>
{:else}
  <div class="relative bg-white dark:bg-slate-950 font-sans">
    <!-- Hero Section -->
    <section class="relative pt-32 pb-16 px-6 overflow-hidden text-center">
      <div class="absolute top-0 left-1/2 -translate-x-1/2 w-full h-[600px] bg-gradient-to-b from-primary/10 to-transparent -z-10 blur-3xl opacity-50"></div>
      <div class="container mx-auto space-y-10 relative z-10">
        <div class="inline-flex items-center gap-3 px-5 py-2 rounded-full bg-slate-900 dark:bg-white text-white dark:text-slate-900 text-[10px] font-black uppercase tracking-[0.3em] shadow-2xl animate-in fade-in slide-in-from-bottom-2 duration-1000">
          <span class="flex h-2 w-2 rounded-full bg-blue-500 animate-pulse"></span>
          Enterprise K8s Security
        </div>
        <h1 class="text-7xl md:text-[120px] font-black tracking-tighter text-slate-900 dark:text-white leading-[0.8] animate-in fade-in zoom-in-95 duration-1000">
          Secure Your <br/> 
          <span class="text-primary italic">k8s Clusters.</span>
        </h1>
        <p class="text-xl md:text-2xl text-slate-500 dark:text-slate-400 font-medium max-w-2xl mx-auto leading-relaxed animate-in fade-in slide-in-from-bottom-4 duration-1000 delay-300">
          실시간 자산 추적부터 지능형 정책 탐지까지, <br/>
          가장 직관적인 쿠버네티스 보안 경험을 시작하세요.
        </p>
        <div class="pt-4 animate-in fade-in slide-in-from-bottom-8 duration-1000 delay-500">
          <a href="/signup" class="h-16 px-12 inline-flex items-center rounded-2xl text-xl font-black bg-primary text-white hover:opacity-90 transition-all active:scale-95 shadow-2xl shadow-primary/30">
            Get Started Free
          </a>
        </div>
      </div>
    </section>

    <!-- Core Values Section -->
    <section class="py-20 px-6">
      <div class="container mx-auto">
        <div class="grid md:grid-cols-3 gap-8">
          {#each coreValues as value, i}
            <div class="animate-on-scroll opacity-0 translate-y-8 transition-all duration-1000 delay-{i*150} p-8 rounded-[2.5rem] bg-slate-50 dark:bg-slate-900/50 border border-slate-100 dark:border-slate-800 group hover:bg-white dark:hover:bg-slate-900 hover:shadow-2xl hover:-translate-y-2 transition-all">
              <div class="w-14 h-14 rounded-2xl bg-primary/10 flex items-center justify-center text-primary mb-6 transition-transform group-hover:scale-110 group-hover:rotate-3 shadow-sm border border-primary/5">
                <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d={value.icon} /></svg>
              </div>
              <h3 class="text-2xl font-black text-slate-900 dark:text-white mb-3 tracking-tight">{value.title}</h3>
              <p class="text-slate-500 dark:text-slate-400 font-medium leading-relaxed">{value.desc}</p>
            </div>
          {/each}
        </div>
      </div>
    </section>

    <!-- Workflow Section -->
    <section class="py-32 px-6 bg-slate-900 text-white overflow-hidden relative border-y border-white/5">
      <div class="container mx-auto space-y-32 relative z-10">
        <div class="text-center space-y-4">
          <h2 class="text-xs font-black uppercase tracking-[0.5em] text-primary">Step-by-Step</h2>
          <h3 class="text-5xl md:text-6xl font-black tracking-tighter italic text-white leading-none uppercase">How it Works.</h3>
        </div>

        <!-- Step 1: Install -->
        <div class="grid md:grid-cols-2 gap-20 items-center animate-on-scroll opacity-0 transition-all duration-1000">
          <div class="space-y-6">
            <div class="text-primary font-black text-6xl opacity-20 tracking-tighter leading-none">01</div>
            <h4 class="text-4xl font-black tracking-tight text-white">Install Agent</h4>
            <p class="text-xl text-slate-400 font-medium leading-relaxed">
              로그인 후 발급 받은 <span class="text-white border-b-2 border-primary/50">전용 API Key</span>를 사용하여 <br/>
              클러스터에 보안 에이전트를 배치합니다.
            </p>
          </div>
          <div class="bg-slate-950 rounded-3xl p-8 border border-white/10 shadow-2xl font-mono text-sm group relative overflow-hidden text-left">
            <div class="flex gap-2 mb-6 border-b border-white/5 pb-4">
              <div class="w-3 h-3 rounded-full bg-rose-500"></div>
              <div class="w-3 h-3 rounded-full bg-amber-500"></div>
              <div class="w-3 h-3 rounded-full bg-emerald-500"></div>
              <span class="ml-4 text-[10px] text-slate-500 uppercase font-black tracking-widest">Cluster Terminal</span>
            </div>
            <div class="space-y-3">
              <div class="flex gap-2 text-slate-300">
                <span class="text-primary/50 font-black">#</span>
                <span>Login to k-secure and copy your API Key</span>
              </div>
              <div class="flex flex-wrap gap-2 text-emerald-400">
                <span class="text-primary/50 font-black">$</span>
                <span>kubectl apply -f https://k-secure.io/agent.yaml</span>
              </div>
              <div class="flex gap-2 text-emerald-400">
                <span class="text-primary/50 font-black">$</span>
                <span>kubectl create secret generic k-secure-key --from-literal=api-key=<span class="bg-primary/20 px-1 rounded text-white animate-pulse">YOUR_API_KEY</span></span>
              </div>
              <div class="pt-4 text-[10px] text-slate-600 leading-relaxed font-mono italic">
                deployment.apps/k-secure-agent created <br/>
                serviceaccount/k-secure-agent created <br/>
                clusterrole.rbac.authorization.k8s.io/k-secure-agent-role created
              </div>
            </div>
          </div>
        </div>

        <!-- Step 2: Auto-Discovery -->
        <div class="grid md:grid-cols-2 gap-20 items-center animate-on-scroll opacity-0 transition-all duration-1000">
          <div class="order-2 md:order-1 relative h-[400px] flex items-center justify-center overflow-hidden rounded-[3rem] bg-slate-950/50 border border-white/5">
            <div class="relative w-full h-full flex items-center justify-center">
              <div class="absolute w-[300px] h-[300px] border border-primary/10 rounded-full animate-ping"></div>
              <div class="relative z-10 grid grid-cols-2 gap-12 scale-90">
                {#each discoveryAssets as asset, i}
                  <div class="flex flex-col items-center gap-3 animate-pulse" style="animation-delay: {i*300}ms">
                    <div class="w-16 h-16 rounded-2xl bg-slate-800 border-2 border-primary/30 flex items-center justify-center shadow-xl transition-transform hover:scale-110">
                      <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" class={asset.color}><path d={asset.icon} /></svg>
                    </div>
                    <div class="px-3 py-1 rounded-full bg-slate-900 border border-white/10 text-[10px] font-black uppercase text-white shadow-2xl">
                      {asset.type}: {asset.name}
                    </div>
                  </div>
                {/each}
              </div>
              <div class="absolute w-1 h-full bg-gradient-to-b from-transparent via-primary/50 to-transparent left-1/2 -translate-x-1/2 animate-scan-line"></div>
            </div>
          </div>
          <div class="space-y-6 order-1 md:order-2">
            <div class="text-primary font-black text-6xl opacity-20 tracking-tighter leading-none">02</div>
            <h4 class="text-4xl font-black tracking-tight text-white">Auto-Discovery Assets</h4>
            <p class="text-xl text-slate-400 font-medium leading-relaxed">
              설치 즉시 클러스터의 Pods, Nodes, Services 정보를 자동으로 스캔하여 실시간 인벤토리를 구축합니다.
            </p>
          </div>
        </div>

        <!-- Step 3: Define Policies -->
        <div class="grid md:grid-cols-2 gap-20 items-center animate-on-scroll opacity-0 transition-all duration-1000">
          <div class="space-y-6">
            <div class="text-primary font-black text-6xl opacity-20 tracking-tighter leading-none">03</div>
            <h4 class="text-4xl font-black tracking-tight text-white tracking-tighter text-left">Define Security Policies</h4>
            <p class="text-xl text-slate-400 font-medium leading-relaxed">
              사전 정의된 룰셋을 바탕으로 위반 사항을 탐지합니다. 사용자 환경에 맞춰 자유롭게 조절하세요.
            </p>
          </div>
          <div class="bg-slate-950 rounded-3xl p-8 border border-white/10 shadow-2xl space-y-6 relative overflow-hidden group">
            <div class="text-[10px] font-black uppercase tracking-[0.2em] text-slate-500 text-left">Policy Control Center</div>
            <div class="space-y-4">
              {#each policyItems as item}
                <div class="p-4 rounded-2xl bg-white/5 border border-white/5 flex items-center justify-between hover:bg-white/10 transition-all">
                  <div class="flex flex-col text-left">
                    <span class="text-sm font-black text-slate-200 tracking-tight">{item.label}</span>
                    <span class="text-[9px] text-slate-500 uppercase font-bold tracking-tighter">{item.rule}</span>
                  </div>
                  <div class="flex items-center gap-4">
                    <span class="text-xs font-black text-primary tabular-nums italic">{item.value}</span>
                    <div class="w-10 h-5 rounded-full {item.enabled ? 'bg-emerald-500' : 'bg-slate-700'} flex items-center {item.enabled ? 'justify-end' : 'justify-start'} px-1 transition-colors">
                      <div class="w-3.5 h-3.5 rounded-full bg-white shadow-lg"></div>
                    </div>
                  </div>
                </div>
              {/each}
            </div>
          </div>
        </div>

        <!-- Step 4: Alerts -->
        <div class="grid md:grid-cols-2 gap-20 items-center animate-on-scroll opacity-0 transition-all duration-1000">
          <div class="order-2 md:order-1 relative">
            <div class="p-8 rounded-[2.5rem] bg-rose-500 text-white shadow-[0_0_60px_rgba(244,63,94,0.4)] animate-bounce relative z-10 border-4 border-rose-400/50">
              <div class="flex items-center gap-4 mb-4">
                <div class="bg-white/20 p-2 rounded-xl backdrop-blur-md"><svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg></div>
                <div class="flex flex-col text-left text-white">
                  <span class="font-black uppercase tracking-[0.2em] text-[10px] text-rose-100">Critical Threat Found</span>
                  <span class="font-black text-2xl tracking-tighter text-white">Security Alert!</span>
                </div>
              </div>
              <div class="p-4 rounded-2xl bg-black/10 border border-white/10 font-mono text-[10px] space-y-1 text-left">
                <div class="flex justify-between"><span class="opacity-60 font-bold uppercase">Target:</span> <span class="font-bold tracking-tighter">deployment/api-server</span></div>
                <div class="flex justify-between"><span class="opacity-60 font-bold uppercase">Violation:</span> <span class="font-bold tracking-tighter">Replica Count &lt; 3</span></div>
              </div>
            </div>
          </div>
          <div class="space-y-6 order-1 md:order-2 text-right">
            <div class="text-primary font-black text-6xl opacity-20 tracking-tighter leading-none">04</div>
            <h4 class="text-4xl font-black tracking-tight text-white leading-none">Real-time Intelligent Alerts</h4>
            <p class="text-xl text-slate-400 font-medium leading-relaxed mt-4">
              위반 사항을 즉시 탐지하고 스마트한 알림을 생성합니다.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="py-40 text-center space-y-12 bg-white dark:bg-slate-950">
      <h2 class="text-6xl md:text-[100px] font-black tracking-tighter text-slate-900 dark:text-white uppercase leading-[0.8] italic">Start <br/> Protecting.</h2>
      <a href="/signup" class="h-20 px-16 inline-flex items-center rounded-3xl text-2xl font-black bg-primary text-white hover:scale-105 transition-all shadow-2xl active:scale-95 shadow-primary/40">
        Get Started Now
      </a>
    </section>
  </div>
{/if}

<style>
  :global(.reveal) {
    opacity: 1 !important;
    transform: translateY(0) !important;
  }
  .animate-spin-slow { animation: spin 15s linear infinite; }
  .animate-scan-line { animation: scan 3s ease-in-out infinite; }
  @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
  @keyframes scan { 
    0% { transform: translateY(-100%) translateX(-50%); opacity: 0; }
    50% { opacity: 1; }
    100% { transform: translateY(100%) translateX(-50%); opacity: 0; }
  }
</style>
