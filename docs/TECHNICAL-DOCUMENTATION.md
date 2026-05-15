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
- de l’appel aux endpoints REST du backend ;
- de l’affichage des fichiers, de l’historique et des actions utilisateur.

### Backend

Le backend Spring Boot est responsable :
- de l’authentification utilisateur ;
- de la génération et validation des JWT ;
- de la logique métier autour des fichiers ;
- de la persistance en base PostgreSQL ;
- du stockage local des fichiers ;
- de l’exposition des endpoints REST;
- de l’exécution des migrations de base de données via Flyway ;
- de la purge planifiée des fichiers expirés ;

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

- `docs/schema_architecture_simple.pdf`

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

### Outils de test et qualité

Les outils retenus sont :
- Maven et Spring Boot Test côté backend ;
- H2 pour les tests backend ;
- JaCoCo pour la couverture backend ;
- k6 pour les mesures de performance backend ;
- Angular TestBed pour les tests unitaires frontend ;
- Cypress pour les tests end-to-end frontend ;
- Lighthouse pour les audits de performance frontend.

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

Cette entité représente un fichier partagé et ses métadonnées.

### Relation

- un utilisateur possède de `0 à n` fichiers ;
- un fichier partagé appartient à `1` utilisateur.

Le modèle de données est fourni dans :
- `docs/ShareFiles_MCD.png`

## 4. Documentation des endpoints principaux

Les endpoints principaux exposés par le backend sont :

### Authentification

- `POST /api/auth/register`
- `POST /api/auth/login`

### Gestion des fichiers authentifiés

- `POST /api/files`
- `GET /api/files`
- `DELETE /api/files/{id}`

### Téléchargement public

- `GET /download/{token}`

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

### Protection des mots de passe

Les mots de passe sont hachés avec BCrypt avant stockage en base.

### Gestion des routes

Les routes sont séparées entre :
- routes publiques :
  - `/api/auth/**`
  - `/download/**`
- routes protégées :
  - `/api/files/**`

### Contrôle d’accès

Certaines opérations métier sont soumises à une vérification de propriété, notamment la suppression de fichier.

### Frontend

Côté frontend :
- un guard protège les routes privées ;
- un interceptor injecte automatiquement le JWT dans les appels API protégés.

Les détails sont documentés dans :
- `SECURITY.md`

## 6. Qualité, tests et maintenance

Le projet s’appuie sur plusieurs documents dédiés :

- `TESTING.md`
- `SECURITY.md`
- `PERF.md`
- `MAINTENANCE.md`

### Tests

À ce stade :
- le backend dispose d’un ensemble de tests unitaires et d’intégration couvrant les services, les contrôleurs, la sécurité et la gestion des erreurs ;
- le frontend dispose de tests unitaires sur le socle applicatif, de tests end-to-end Cypress sur les parcours critiques et de validations manuelles complémentaires ;
- les parcours critiques ont été validés manuellement côté frontend et backend.

### Performance

Une première mesure de performance backend a été réalisée avec k6 sur l’endpoint `POST /api/files`.

### Maintenance

La maintenance est documentée séparément pour faciliter :
- les évolutions ;
- les corrections ;
- la compréhension des zones sensibles.

## 7. Processus d’installation et d’exécution

### Backend

Le backend nécessite :
- Java 21 ;
- Maven ;
- Docker ;
- Docker Compose ;
- un fichier `.env`.

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

### Environnement

En développement local, le frontend communique avec le backend via le proxy Angular.
La configuration de production repose sur des chemins relatifs, afin d’éviter toute dépendance à une URL `localhost` codée en dur.

Les instructions détaillées sont disponibles dans :
- `README.md` du repo backend
- `README.md` du repo frontend

## 8. Utilisation de l’IA dans le développement

L’intelligence artificielle a été utilisée comme assistant de développement sur une user story ciblée.

Le document dédié :
`AI-USAGE.md`

précise :

- les tâches confiées à l’IA ;
- le rôle de supervision humaine ;
- les ajustements réalisés avant intégration ;
- la traçabilité dans l’historique Git.

L’IA a été utilisée comme outil d’assistance, et non comme mécanisme d’intégration automatique sans validation.

## 9. Limites actuelles et évolutions possibles

Le projet est actuellement un MVP fonctionnel.
Les principales limites identifiées sont :

- la couverture de branches backend reste perfectible ;
- les tests frontend restent plus limités que les tests backend ;
- l’observabilité reste encore partielle ;
- l’architecture de stockage reste volontairement simple pour un MVP ;
- les performances mobiles du frontend restent en retrait par rapport au desktop.

Les évolutions possibles incluent :

- augmentation de la couverture de tests ;
- amélioration de la gestion des exceptions ;
- validation plus stricte des fichiers téléversés ;
- amélioration de la robustesse sécurité ;
- enrichissement des fonctionnalités de partage sécurisé.
