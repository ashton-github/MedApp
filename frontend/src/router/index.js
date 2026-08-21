import { createRouter, createWebHistory } from 'vue-router'
import routeDefinitions from './routes.js'
import { registerGuards } from './guards.js'

// ─── Router ───────────────────────────────────────────────────────────────────
// This file is intentionally thin — it only wires together the two modules
// that contain the actual logic:
//
//  • routes.js  → all route definitions (lazy-loaded components, meta, props)
//  • guards.js  → all navigation guards (auth, role checks)
//
// To add a route: edit routes.js only.
// To change access rules: edit guards.js only.
// ─────────────────────────────────────────────────────────────────────────────

export const router = createRouter({
    history: createWebHistory(),
    routes: routeDefinitions
})

registerGuards(router)

export default router
