import { useAuthStore } from '../stores/authStore.js'

// ─── Navigation Guards ────────────────────────────────────────────────────────
// Registers all global router guards on the provided router instance.
//
// Guards applied (in order):
//  1. Redirect unauthenticated users away from protected routes → /login
//  2. Redirect authenticated users away from /login → /dashboard
//  3. Redirect users without the required role → /dashboard
//
// Usage:
//   import { registerGuards } from './guards.js'
//   registerGuards(router)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Registers all global beforeEach guards on `router`.
 * @param {import('vue-router').Router} router
 */
export function registerGuards(router) {
    router.beforeEach((to) => {
        const authStore = useAuthStore()

        // meta.requiresAuth is true by default — only routes that explicitly set
        // it to `false` are considered public (e.g. /login, /register).
        const requiresAuth = to.meta.requiresAuth !== false

        // 1. Not authenticated and trying to reach a protected route.
        //    Remember where they were headed so we can redirect after login.
        if (requiresAuth && !authStore.isAuthenticated) {
            if (to.name === 'login') return true
            return { name: 'login', query: { redirect: to.fullPath } }
        }

        // 2. Already authenticated and trying to reach a public auth page.
        if ((to.name === 'login' || to.name === 'register') && authStore.isAuthenticated) {
            return { name: 'dashboard' }
        }

        // 3. Role-restricted route: bounce back to dashboard if the role
        //    is not listed in meta.roles.
        if (to.meta.roles && !to.meta.roles.includes(authStore.role)) {
            return { name: 'dashboard' }
        }

        return true
    })
}
