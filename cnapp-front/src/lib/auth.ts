import { writable } from 'svelte/store';
import { browser } from '$app/environment';

// 초기값 설정 함수
function getInitialValue(key: string): string | null {
    if (browser) {
        return localStorage.getItem(key);
    }
    return null;
}

const initialToken = getInitialValue('token');
const initialUsername = getInitialValue('username');

export const authToken = writable<string | null>(initialToken);
export const user = writable<{ username: string } | null>(initialUsername ? { username: initialUsername } : null);
export const isAuthenticated = writable<boolean>(!!initialToken);

export function setAuthToken(token: string, username?: string) {
    if (browser) {
        localStorage.setItem('token', token);
        if (username) {
            localStorage.setItem('username', username);
            user.set({ username });
        }
    }
    authToken.set(token);
    isAuthenticated.set(true);
}

export function clearAuthToken() {
    if (browser) {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
    }
    authToken.set(null);
    isAuthenticated.set(false);
    user.set(null);
}

export function checkAuth() {
    if (browser) {
        const token = localStorage.getItem('token');
        const username = localStorage.getItem('username');
        if (token) {
            authToken.set(token);
            isAuthenticated.set(true);
            if (username) {
                user.set({ username });
            }
        } else {
            authToken.set(null);
            isAuthenticated.set(false);
            user.set(null);
        }
    }
}
