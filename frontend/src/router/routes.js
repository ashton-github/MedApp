import { screens } from '../constants/medapp.js'

// ─── Route Definitions ────────────────────────────────────────────────────────
// Single source of truth for all application routes.
//
// Rules:
//  • Components are lazy-loaded via dynamic import() → Vite produces one chunk
//    per route, keeping the initial bundle small.
//  • meta.requiresAuth defaults to true. Set it to `false` only for public routes.
//  • meta.screen maps the route back to the `screens.*` constant so that
//    existing components (Sidebar, App.vue) that compare against screens.*
//    keep working unchanged.
//  • meta.roles, when present, restricts the route to those frontend roles.
//  • props: true forwards route params as component props (e.g. :id).
//
// To add a new route: add ONE object here — no other file needs to change.
// ─────────────────────────────────────────────────────────────────────────────

/** @type {import('vue-router').RouteRecordRaw[]} */
const routeDefinitions = [
    // ── Redirect root to dashboard ───────────────────────────────────────────
    { path: '/', redirect: '/dashboard' },

    // ── Auth ─────────────────────────────────────────────────────────────────
    {
        path: '/login',
        name: 'login',
        component: () => import('../components/screens/LoginScreen.vue'),
        meta: { requiresAuth: false, screen: screens.login }
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('../components/screens/RegisterView.vue'),
        meta: { requiresAuth: false, screen: screens.login }
    },

    // ── Dashboard ────────────────────────────────────────────────────────────
    {
        path: '/dashboard',
        name: 'dashboard',
        component: () => import('../components/screens/DashboardScreen.vue'),
        meta: { screen: screens.dashboard }
    },

    // ── Patients ─────────────────────────────────────────────────────────────
    {
        path: '/patients',
        name: 'patients',
        component: () => import('../components/screens/PatientsScreen.vue'),
        meta: { screen: screens.patients }
    },
    {
        path: '/patients/nouveau',
        name: 'patient-new',
        component: () => import('../components/screens/PatientFormScreen.vue'),
        meta: { screen: screens.patientForm }
    },
    {
        path: '/patients/:id',
        name: 'patient-detail',
        component: () => import('../components/screens/PatientDetailScreen.vue'),
        props: true,
        meta: { screen: screens.patientDetail }
    },
    {
        path: '/patients/:id/modifier',
        name: 'patient-edit',
        component: () => import('../components/screens/PatientFormScreen.vue'),
        props: true,
        meta: { screen: screens.patientForm }
    },

    // ── Ordonnances ───────────────────────────────────────────────────────────
    {
        path: '/ordonnances',
        name: 'ordonnances',
        component: () => import('../components/screens/OrdonnancesScreen.vue'),
        meta: { screen: screens.ordonnances }
    },
    {
        path: '/ordonnances/nouvelle',
        name: 'ordonnance-new',
        component: () => import('../components/screens/OrdonnanceFormScreen.vue'),
        meta: { screen: screens.ordonnanceForm }
    },
    {
        path: '/ordonnances/:id/modifier',
        name: 'ordonnance-edit',
        component: () => import('../components/screens/OrdonnanceFormScreen.vue'),
        props: true,
        meta: { screen: screens.ordonnanceForm }
    },
    {
        path: '/ordonnances/apercu',
        name: 'pdf-preview',
        component: () => import('../components/screens/PDFPreviewScreen.vue'),
        meta: { screen: screens.pdfPreview }
    },

    // ── Agenda ────────────────────────────────────────────────────────────────
    {
        path: '/agenda',
        name: 'agenda',
        component: () => import('../components/screens/AgendaScreen.vue'),
        meta: { screen: screens.agenda }
    },

    // ── Paramètres ────────────────────────────────────────────────────────────
    {
        path: '/parametres',
        name: 'settings',
        component: () => import('../components/screens/SettingsScreen.vue'),
        meta: { screen: screens.settings }
    },

    // ── Catch-all — redirect unmatched URLs to dashboard ─────────────────────
    // The navigation guard will redirect further to /login if unauthenticated.
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

export default routeDefinitions
