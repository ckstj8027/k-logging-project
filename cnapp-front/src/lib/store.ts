import { writable } from 'svelte/store';
import api from './api';
import type { DashboardSummary } from './types';

export const dashboardSummary = writable<DashboardSummary | null>(null);
export const isSummaryLoading = writable(false);

export async function fetchDashboardSummary() {
    isSummaryLoading.set(true);
    try {
        const res = await api.get('/dashboard');
        dashboardSummary.set(res.data);
    } catch (e) {
        console.error('Failed to fetch dashboard summary', e);
    } finally {
        isSummaryLoading.set(false);
    }
}
