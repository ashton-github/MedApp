# MedApp — Environnement de développement Docker

Stack minimale pour démarrer le stage : **VueJS + Spring Boot + MongoDB**, en mode
développement (hot-reload), prête pour appliquer le TDD dès le premier jour.

## Contenu

```
docker-stack/
├── docker-compose.yml       # orchestration des 4 services
├── backend/                 # Spring Boot (API REST) — port 8080
├── frontend/                # Vue.js 3 + Vite — port 5173
└── mongo-init/              # script d'init MongoDB (collections + index)
```

## Services démarrés

| Service        | URL                              | Rôle                                    |
|----------------|-----------------------------------|------------------------------------------|
| frontend       | http://localhost:5173             | Application Vue.js (hot-reload)          |
| backend        | http://localhost:8080/api/health  | API Spring Boot (hot-reload)             |
| mongodb        | mongodb://localhost:27017/medapp  | Base de données                          |
| mongo-express  | http://localhost:8081             | Interface d'administration MongoDB (admin/admin) |

## Démarrage

Prérequis : Docker et Docker Compose installés.

```bash
cd docker-stack
docker compose up --build
```

Premier démarrage plus long (téléchargement des dépendances Maven/npm).
Une fois lancé :

- Ouvrir http://localhost:5173 → la page doit afficher **Backend : UP** et **MongoDB : UP**.
- Tester l'API directement : `curl http://localhost:8080/api/health`

## Développement (hot-reload)

Le code source de `backend/src` et de `frontend/` est monté en volume :
toute modification est automatiquement prise en compte, sans reconstruire les images
(Spring DevTools côté backend, serveur Vite côté frontend).

## Lancer les tests (TDD)

**Backend** (dans un terminal, conteneur backend déjà démarré) :
```bash
docker compose exec backend mvn test
```

**Frontend** :
```bash
docker compose exec frontend npm run test
```

## Ce qui est déjà en place (le minimum pour démarrer)

- Connexion Spring Boot ↔ MongoDB fonctionnelle.
- Un endpoint `GET /api/health` qui vérifie que l'API et MongoDB répondent.
- CORS configuré pour autoriser le frontend (`localhost:5173`) à appeler l'API.
- Une configuration de sécurité **volontairement permissive** (`SecurityConfig.java`),
  à remplacer en Semaine 2 par une authentification JWT complète.
- Un `PasswordEncoder` (BCrypt) déjà exposé, prêt pour les premiers tests du
  service d'inscription.
- Un exemple de test backend (`BackendApplicationTests`) et un exemple de test
  frontend (`App.spec.js`) illustrant le cycle Red-Green-Refactor.
- Les collections MongoDB `users`, `patients`, `ordonnances` créées avec leurs
  index de base au premier démarrage.

## Ce qu'il reste à développer (objet du stage)

- Authentification JWT complète (inscription, connexion, rôles).
- CRUD patients (backend + interface).
- Création et gestion des ordonnances numériques.

Chaque fonctionnalité doit être développée en écrivant d'abord le test
(Red), puis le code minimal pour le faire passer (Green), puis en
améliorant le code (Refactor).

## Arrêter l'environnement

```bash
docker compose down          # arrête les conteneurs
docker compose down -v       # arrête et supprime aussi les volumes (perte des données Mongo)
```
