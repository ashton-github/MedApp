import { createRouter, createWebHistory } from 'vue-router'
import { screens } from '../constants/medapp.js'
import { useAuthStore } from '../stores/authStore.js'

import LoginScreen from '../components/screens/LoginScreen.vue'
import DashboardScreen from '../components/screens/DashboardScreen.vue'
import PatientsScreen from '../components/screens/PatientsScreen.vue'
import PatientFormScreen from '../components/screens/PatientFormScreen.vue'
import PatientDetailScreen from '../components/screens/PatientDetailScreen.vue'
import OrdonnancesScreen from '../components/screens/OrdonnancesScreen.vue'
import OrdonnanceFormScreen from '../components/screens/OrdonnanceFormScreen.vue'
import PDFPreviewScreen from '../components/screens/PDFPreviewScreen.vue'
import AgendaScreen from '../components/screens/AgendaScreen.vue'
import SettingsScreen from '../components/screens/SettingsScreen.vue'

// meta.screen keeps each route mapped back to the legacy `screens.*` constant
// so existing components (Sidebar, App.vue) that compare against screens.*
// keep working unchanged.
// meta.requiresAuth defaults to true — only /login is public.
// meta.roles, when present, restricts the route to those frontend roles.
const routes = [
    { path: '/', redirect: '/dashboard' },
    {
        path: '/login',
        name: 'login',
        component: LoginScreen,
        meta: { requiresAuth: false, screen: screens.login }
    },
    {
        path: '/dashboard',
        name: 'dashboard',
        component: DashboardScreen,
        meta: { screen: screens.dashboard }
    },
    {
        path: '/patients',
        name: 'patients',
        component: PatientsScreen,
        meta: { screen: screens.patients }
    },
    {
        path: '/patients/nouveau',
        name: 'patient-new',
        component: PatientFormScreen,
        meta: { screen: screens.patientForm }
    },
    {
        path: '/patients/:id',
        name: 'patient-detail',
        component: PatientDetailScreen,
        props: true,
        meta: { screen: screens.patientDetail }
    },
    {
        path: '/patients/:id/modifier',
        name: 'patient-edit',
        component: PatientFormScreen,
        props: true,
        meta: { screen: screens.patientForm }
    },
    {
        path: '/ordonnances',
        name: 'ordonnances',
        component: OrdonnancesScreen,
        meta: { screen: screens.ordonnances }
    },
    {
        path: '/ordonnances/nouvelle',
        name: 'ordonnance-new',
        component: OrdonnanceFormScreen,
        meta: { screen: screens.ordonnanceForm }
    },
    {
        path: '/ordonnances/:id/modifier',
        name: 'ordonnance-edit',
        component: OrdonnanceFormScreen,
        props: true,
        meta: { screen: screens.ordonnanceForm }
    },
    {
        path: '/ordonnances/apercu',
        name: 'pdf-preview',
        component: PDFPreviewScreen,
        meta: { screen: screens.pdfPreview }
    },
    {
        path: '/agenda',
        name: 'agenda',
        component: AgendaScreen,
        meta: { screen: screens.agenda }
    },
    {
        path: '/parametres',
        name: 'settings',
        component: SettingsScreen,
        meta: { screen: screens.settings }
    },
    // Any unmatched URL falls back to the dashboard (the guard below will
    // redirect further to /login if the user isn't authenticated).
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

export const router = createRouter({
    history: createWebHistory(),
    routes
})

// ─── Navigation guard ────────────────────────────────────────────────────────
// Blocks access to protected routes based on role and login status.
router.beforeEach((to) => {
    const authStore = useAuthStore()

    const requiresAuth = to.meta.requiresAuth !== false

    // Not logged in and trying to reach a protected route → send to login,
    // remembering where they were headed.
    if (requiresAuth && !authStore.isAuthenticated) {
        if (to.name === 'login') return true
        return { name: 'login', query: { redirect: to.fullPath } }
    }

    // Already logged in and trying to reach /login → send to dashboard.
    if (to.name === 'login' && authStore.isAuthenticated) {
        return { name: 'dashboard' }
    }

    // Role-restricted route: bounce back to dashboard if the role doesn't match.
    if (to.meta.roles && !to.meta.roles.includes(authStore.role)) {
        return { name: 'dashboard' }
    }

    return true
})

export default router
