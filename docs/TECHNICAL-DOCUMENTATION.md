# TECHNICAL-DOCUMENTATION.md

## 1. Architecture de l’application

DataShare repose sur une architecture web séparée en deux parties :

- un frontend Angular ;
- un backend Spring Boot ;
- une base de données PostgreSQL ;
- un stockage local pour les fichiers téléversés.

### Frontend

Le frontend Angular est responsable :
- de l’interface utilisateur ;
- de la navigation entre les pages ;
- de l’authentification côté client ;
- de la protection des routes ;
- de la gestion de la session côté navigateur ;
- de l’appel aux endpoints REST du backend ;
- de l’affichage des fichiers, de l’historique et des actions utilisateur.

### Backend

Le backend Spring Boot est responsable :
- de l’authentification utilisateur ;
- de la génération et validation des JWT ;
- de la gestion des JWT invalides, expirés ou malformés ;
- de la logique métier autour des fichiers ;
- de la validation serveur des fichiers téléversés ;
- de la persistance en base PostgreSQL ;
- du stockage local des fichiers ;
- de l’exposition des endpoints REST ;
- de l’exécution des migrations de base de données via Flyway ;
- de la purge planifiée des fichiers expirés ;
- de la configuration CORS externalisée.

### Base de données

PostgreSQL stocke :
- les utilisateurs ;
- les métadonnées des fichiers partagés.

### Stockage local

Les fichiers téléversés sont stockés sur disque dans le répertoire défini par `UPLOAD_DIR`.

### Flux principal

1. l’utilisateur interagit avec le frontend Angular ;
2. le frontend appelle le backend via HTTP ;
3. le backend applique les règles métier et de sécurité ;
4. les métadonnées sont enregistrées en base ;
5. le fichier est stocké localement ;
6. le frontend restitue le résultat à l’utilisateur.

Le diagramme d’architecture est fourni dans :

- `docs/architecture-datashare.png`

## 2. Choix technologiques justifiés

### Backend : Spring Boot

Le backend a été développé avec Spring Boot car il permet de structurer rapidement une API REST robuste avec :
- Spring Web pour les endpoints HTTP ;
- Spring Security pour la sécurité ;
- Spring Data JPA pour l’accès aux données.

Ce choix est adapté à un projet de type API métier avec authentification, persistance et logique de service.

### Frontend : Angular

Le frontend a été développé avec Angular pour bénéficier :
- d’une structure de projet claire ;
- du routage intégré ;
- des services HTTP ;
- d’une séparation nette entre composants, services, guards et interceptors.

Angular est adapté à un projet de type application métier avec plusieurs écrans protégés et une logique de navigation claire.

Le frontend utilise un routage avec chargement différé des pages principales et une gestion d’état réactive sur les écrans critiques, afin de limiter le poids initial du bundle et de fiabiliser le rendu après opérations asynchrones.

### Base de données : PostgreSQL

PostgreSQL a été retenu comme base relationnelle principale car il est :
- robuste ;
- largement utilisé ;
- bien intégré à Spring Boot ;
- adapté au stockage des utilisateurs et métadonnées de fichiers.

### Stockage local

Le choix du stockage local permet de garder un MVP simple à déployer et à comprendre.  
Il évite d’introduire trop tôt une dépendance à un stockage objet externe.

Ce choix est cohérent avec un prototype fonctionnel, même s’il devra évoluer pour une version plus industrialisée.

### JWT

Le JWT a été retenu pour :
- l’authentification stateless ;
- la séparation claire entre frontend et backend ;
- la simplicité d’intégration dans un MVP web moderne.

### Docker Compose

Docker Compose est utilisé pour simplifier le démarrage local de PostgreSQL et rendre l’environnement plus reproductible.

### Outils de test, qualité et sécurité

Les outils retenus sont :
- Maven et Spring Boot Test côté backend ;
- H2 pour les tests backend ;
- JaCoCo pour la couverture backend ;
- OWASP Dependency Check pour le scan des dépendances backend ;
- k6 pour les mesures de performance backend ;
- Angular TestBed avec exécution via Vitest côté frontend ;
- Cypress pour les tests end-to-end frontend ;
- Lighthouse pour les audits de performance frontend.

Côté frontend, Angular s’appuie sur Vitest pour l’exécution des tests unitaires.
Les tests utilisent `TestBed` pour l’intégration avec l’écosystème Angular, tandis que l’exécution et la couverture sont réalisées via `ng test` et le moteur Vitest/V8.

Le scan OWASP Dependency Check permet d’identifier les vulnérabilités connues sur les dépendances directes et transitives du backend. Les résultats sont documentés dans `SECURITY.md`.

## 3. Modèle de données

Le modèle de données repose sur deux entités principales.

### UTILISATEUR

Champs principaux :
- `id`
- `email`
- `password_hash`
- `created_at`
- `updated_at`

Cette entité représente un compte utilisateur authentifiable.

### FICHIER_PARTAGE

Champs principaux :
- `id`
- `original_name`
- `stored_name`
- `content_type`
- `size`
- `download_token`
- `expires_at`
- `password_hash`
- `created_at`
- `updated_at`
- `owner_id`

Cette entité représente un fichier partagé et ses métadonnées.

Le champ `password_hash` correspond à l’anticipation technique de l’US09, qui concerne la protection optionnelle d’un fichier par mot de passe. Cette fonctionnalité n’est pas livrée dans le périmètre MVP actuel.

### Relation

- un utilisateur possède de `0 à n` fichiers ;
- un fichier partagé appartient à `1` utilisateur.

Le modèle de données est fourni dans :
- `docs/mcd-datashare.png`

## 4. Documentation des endpoints principaux

Les endpoints principaux exposés par le backend sont :

### Authentification

- `POST /api/auth/register` : création d’un compte utilisateur ;
- `POST /api/auth/login` : connexion utilisateur et génération d’un JWT.

### Gestion des fichiers authentifiés

- `POST /api/files` : upload d’un fichier par un utilisateur connecté ;
- `GET /api/files` : récupération de l’historique de l’utilisateur connecté ;
- `DELETE /api/files/{id}` : suppression d’un fichier appartenant à l’utilisateur connecté ;
- `POST /api/files/bulk-delete` : suppression multiple de fichiers appartenant à l’utilisateur connecté.

La réponse d’upload contient notamment :
- l’identifiant du fichier ;
- le nom original ;
- la taille du fichier ;
- le token public de téléchargement ;
- l’URL de téléchargement ;
- la date d’expiration.

### Téléchargement public

- `GET /download/{token}` : téléchargement public d’un fichier via son token.

Une documentation OpenAPI est exposée dynamiquement par le backend via springdoc-openapi.

Elle est accessible localement via :
- `/v3/api-docs`
- `/v3/api-docs.yaml`
- `/swagger-ui/index.html`

Un fichier documentaire est également conservé dans :
- `docs/openapi.yaml`

## 5. Sécurité et gestion des accès

### Authentification

L’authentification repose sur un JWT généré lors de la connexion.

Le backend fonctionne en mode stateless :
- pas de session serveur ;
- JWT transmis par le frontend dans l’en-tête `Authorization: Bearer <token>` ;
- authentification HTTP Basic désactivée ;
- formulaire de login Spring Security désactivé.

Les JWT invalides, expirés ou malformés sont gérés sans provoquer d’erreur serveur. Les routes protégées répondent alors avec un statut `401 Unauthorized`.

### Protection des mots de passe

Les mots de passe utilisateurs sont hachés avec BCrypt avant stockage en base.

### Gestion des routes

Les routes sont séparées entre :
- routes publiques :
  - `/api/auth/**`
  - `/download/**`
  - `/v3/api-docs/**`
  - `/swagger-ui/**`
  - `/swagger-ui.html`
- routes protégées :
  - `/api/files/**`

### Contrôle d’accès

Certaines opérations métier sont soumises à une vérification de propriété, notamment la suppression unitaire et multiple de fichiers.

Le backend distingue :
- `401 Unauthorized` lorsqu’un utilisateur n’est pas authentifié ou présente un JWT invalide ;
- `403 Forbidden` lorsqu’un utilisateur authentifié tente d’accéder à une ressource qui ne lui appartient pas.

### Sécurité des fichiers

Les fichiers téléversés sont contrôlés côté backend :
- rejet des fichiers vides ;
- contrôle de la taille maximale ;
- contrôle du type MIME ;
- nettoyage du nom original ;
- stockage sous un nom technique basé sur un UUID ;
- vérification du chemin de stockage pour limiter le risque de path traversal.

Les fichiers ne sont pas stockés en clair sous leur nom original. Ils sont stockés sous un nom technique dans `UPLOAD_DIR`, tandis que l’accès public repose sur un token non prédictible et une date d’expiration.

### CORS

La configuration CORS est explicite et externalisée via la variable :

```env
APP_CORS_ALLOWED_ORIGINS=http://localhost:4200
```

Cette valeur peut être adaptée par environnement sans modification du code Java.

### Frontend

Côté frontend :

- un guard protège les routes privées ;
- un interceptor injecte automatiquement le JWT dans les appels API protégés ;
- une réponse `401` hors endpoints publics entraîne la suppression de la session locale ;
- l’utilisateur est redirigé vers `/login` avec conservation de la route d’origine ;
- un message explicatif est affiché lorsque la session a expiré.

Les détails sont documentés dans :

- `SECURITY.md`

## 6. Qualité, tests et maintenance

Le projet s’appuie sur plusieurs documents dédiés :

- `TESTING.md`
- `SECURITY.md`
- `PERF.md`
- `MAINTENANCE.md`
- `docs/AI-USAGE.md`

### Tests

À ce stade :
- le backend dispose d’un ensemble de tests unitaires et d’intégration couvrant les services, les contrôleurs, la sécurité, la gestion des erreurs, la suppression multiple de fichiers, la validation d’upload et les JWT invalides ;
- le frontend dispose de tests unitaires exécutés avec Vitest sur le socle applicatif, les services, le guard, l’interceptor et les principales pages métier ;
- le frontend couvre notamment la validation des fichiers côté upload, le formatage des tailles, la logique de sélection multiple et la suppression groupée dans l’historique ;
- le frontend dispose également de tests end-to-end Cypress sur les parcours critiques ainsi que de validations manuelles complémentaires ;
- les parcours critiques ont été validés manuellement côté frontend et backend.

Les chiffres de tests et de couverture sont centralisés dans :
- `TESTING.md`

### Performance

Des mesures de performance backend ont été réalisées avec k6 sur l’endpoint `POST /api/files`.

Les scénarios couvrent plusieurs tailles de fichiers et un scénario de stress court jusqu’à 40 utilisateurs virtuels.

Les résultats détaillés sont centralisés dans :
- `PERF.md`

### Maintenance

La maintenance est documentée séparément pour faciliter :
- les évolutions ;
- les corrections ;
- la compréhension des zones sensibles ;
- le suivi des dépendances ;
- le suivi des vulnérabilités ;
- la gestion des migrations Flyway ;
- la cohérence entre base de données et stockage local.

Les règles de maintenance sont décrites dans :
- `MAINTENANCE.md`

## 7. Processus d’installation et d’exécution

### Backend

Le backend nécessite :
- Java 21 ;
- Maven ;
- Docker ;
- Docker Compose ;
- un fichier `.env`.

Le fichier `.env` doit être créé à partir de `.env.example`.

Variables principales :
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`
- `UPLOAD_DIR`
- `APP_CORS_ALLOWED_ORIGINS`

Le lancement local se fait avec :

```bash
./run-local.sh
``` 

### Frontend

Le frontend nécessite :
- Node.js ;
- npm ;
- Angular CLI.

Le lancement local se fait avec :

```bash
npm install
ng serve
```

Les vérifications principales côté frontend se font avec :

```bash
npm run test
npm run test:coverage
npm run cy:run
```

### Environnement

En développement local, le frontend communique avec le backend via le proxy Angular.

Côté backend, les origines CORS autorisées sont externalisées via `APP_CORS_ALLOWED_ORIGINS`.

Côté frontend, les URLs backend sont centralisées dans la configuration d’environnement et le proxy de développement, afin d’éviter la dispersion d’URLs codées en dur dans le code applicatif.

En production, les valeurs devront être adaptées au domaine réel du frontend et au mode de déploiement retenu.

Les instructions détaillées sont disponibles dans :
- `README.md` du repo backend
- `README.md` du repo frontend

## 8. Utilisation de l’IA dans le développement

L’intelligence artificielle a été utilisée comme assistant de développement sur des évolutions ciblées du projet, avec supervision humaine systématique.

Le document dédié à l’usage de l’intelligence artificielle dans le développement est disponible dans :
- `docs/AI-USAGE.md`

Il précise :

- les tâches confiées à l’IA ;
- le rôle de supervision humaine ;
- les ajustements réalisés avant intégration ;
- les limites de l’assistance IA ;
- la traçabilité dans l’historique Git.

## 9. Limites actuelles et évolutions possibles

Le projet est actuellement un MVP fonctionnel.
Les principales limites identifiées sont :

- la couverture de branches reste perfectible ;
- la pagination de l’historique des fichiers n’est pas encore mise en place ;
- le rate limiting de l’endpoint de connexion n’est pas encore implémenté ;
- le JWT est stocké côté frontend en `localStorage`, solution simple pour le MVP mais à durcir pour une mise en production ;
- Swagger UI devra être désactivé ou protégé hors environnement de développement ;
- l’observabilité reste encore partielle ;
- l’architecture de stockage reste volontairement simple pour un MVP ;
- les performances mobiles du frontend restent en retrait par rapport au desktop ;
- la protection optionnelle des fichiers par mot de passe, prévue comme évolution fonctionnelle, n’est pas livrée dans le MVP.

Les évolutions possibles incluent :

- ajout d’une pagination sur `GET /api/files` ;
- ajout d’un rate limiting sur `POST /api/auth/login` ;
- durcissement de la stratégie de stockage du JWT côté frontend ;
- désactivation ou protection de Swagger UI en production ;
- automatisation de la veille dépendances avec Dependabot, Renovate ou une étape CI ;
- amélioration de l’observabilité avec logs structurés, identifiant de corrélation et métriques applicatives ;
- évolution du stockage local vers un stockage objet externe ;
- ajout d’une protection optionnelle par mot de passe pour les liens de téléchargement.
