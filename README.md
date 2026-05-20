# DataShare Backend

Backend Spring Boot de l’application DataShare.

## Description

Ce service expose l’API REST de l’application de partage sécurisé de fichiers.  
Il gère :
- l’authentification des utilisateurs ;
- l’upload de fichiers pour les utilisateurs connectés ;
- l’historique des fichiers envoyés ;
- le téléchargement public via un token ;
- la suppression unitaire ou multiple des fichiers par leur propriétaire ;
- la persistance PostgreSQL ;
- le stockage local des fichiers uploadés ;
- la documentation OpenAPI générée via springdoc.

## Stack technique

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- BCrypt
- PostgreSQL
- Flyway
- Docker Compose
- Lombok
- springdoc-openapi
- JaCoCo
- OWASP Dependency Check

## Pré-requis

- JDK 21
- Maven
- Docker
- Docker Compose

## Configuration

La configuration locale repose sur un fichier `.env`.
Un fichier `.env.example` est fourni pour documenter les variables attendues.

Exemple :

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=datashare_db
DB_USER=datashare_user
DB_PASSWORD=datashare_password

JWT_SECRET=change-me-with-a-long-secret-key
JWT_EXPIRATION_MS=3600000

UPLOAD_DIR=uploads

APP_CORS_ALLOWED_ORIGINS=http://localhost:4200
```

Créer ensuite un fichier `.env` local à partir de ce modèle.

La variable `APP_CORS_ALLOWED_ORIGINS` permet de configurer les origines frontend autorisées pour les requêtes CORS.  
En local, elle pointe vers l’application Angular :

```env
APP_CORS_ALLOWED_ORIGINS=http://localhost:4200
```

En production, cette valeur doit être remplacée par le domaine réel du frontend sans modification du code Java.

## Démarrage en local

Le projet contient un script `run-local.sh` qui :

- démarre PostgreSQL avec Docker Compose; 
- charge les variables d’environnement depuis `.env`; 
- attend que PostgreSQL soit prêt; 
- lance l’application Spring Boot;

Commande :

```bash
./run-local.sh
```

L’application est disponible sur `http://localhost:8080`.   

## Premier démarrage attendu

Lors du premier démarrage, le script :

- lance le conteneur PostgreSQL; 
- attend que la base soit accessible; 
- démarre ensuite l’application backend;

Exemple de traces attendues :

```bash
Démarrage de l'application en local avec Docker Compose...
Définition des variables d'environnement à partir du fichier .env...
Attente de PostgreSQL sur localhost:5432...
PostgreSQL est prêt.
...
Tomcat started on port 8080 (http) with context path '/'
Started DatashareBackendApplication
```

## Base de données

Le backend utilise PostgreSQL.
Le conteneur est défini dans `compose.yaml`.
Les données sont persistées dans un volume Docker dédié.

Le schéma est géré par **Flyway** via des migrations SQL versionnées.
Hibernate est configuré en mode validation du schéma.

## Stockage local des fichiers

Les fichiers uploadés sont stockés dans le répertoire défini par la variable `UPLOAD_DIR`.
En local, ce répertoire peut être par exemple :

```env
UPLOAD_DIR=uploads
```

Le dossier est créé automatiquement au démarrage si nécessaire.
Le répertoire `uploads/` n’est pas versionné dans Git car il contient des fichiers générés à l’exécution.

## Endpoints principaux

### Authentification

- `POST /api/auth/register` : création d’un compte utilisateur
- `POST /api/auth/login` : connexion et génération d’un JWT

### Fichiers authentifiés

- `POST /api/files` : upload d’un fichier par un utilisateur connecté
- `GET /api/files` : historique des fichiers de l’utilisateur connecté
- `DELETE /api/files/{id}` : suppression d’un fichier appartenant à l’utilisateur connecté
- `POST /api/files/bulk-delete` : suppression multiple de fichiers appartenant à l’utilisateur connecté

### Téléchargement public

- `GET /download/{token}` : téléchargement public d’un fichier via son token

Les routes `/api/files/**` nécessitent un JWT valide transmis dans l’en-tête :

```http
Authorization: Bearer <token>
```

Les routes `/api/auth/**` et `/download/**` sont publiques.

## Documentation API

La documentation OpenAPI est générée dynamiquement via springdoc-openapi.

Elle est accessible localement via :

- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`
- `http://localhost:8080/v3/api-docs.yaml`

Un fichier documentaire complémentaire peut également être conservé dans :

- `docs/openapi.yaml`

Après modification du contrat API, il faut vérifier que Swagger UI et le fichier documentaire restent cohérents avec le code.

Points à vérifier en priorité :

- schémas de requête/réponse ;
- codes HTTP ;
- endpoints protégés ou publics ;
- messages d’erreur ;
- DTO retournés par l’upload, notamment le champ `size`.

## Documentation complémentaire

Les documents complémentaires du projet sont disponibles dans `docs/`, notamment :

- `docs/TECHNICAL-DOCUMENTATION.md`
- `docs/AI-USAGE.md`
- `docs/openapi.yaml`
- `docs/security/maven-dependency-tree.txt`
- `docs/security/dependency-check/dependency-check-report.html`
- `docs/security/dependency-check/dependency-check-report.json`

Les fichiers de suivi qualité et maintenance sont également présents à la racine du projet :

- `TESTING.md`
- `SECURITY.md`
- `PERF.md`
- `MAINTENANCE.md`

## Sécurité

- authentification par JWT stateless ;
- mots de passe utilisateurs stockés hashés avec BCrypt ;
- authentification HTTP Basic et formulaire Spring Security désactivés ;
- routes `/api/auth/**` et `/download/**` publiques ;
- routes `/api/files/**` protégées ;
- gestion des JWT invalides, expirés ou malformés avec réponse `401` ;
- distinction entre `401 Unauthorized` et `403 Forbidden` ;
- configuration CORS explicite et externalisée via `APP_CORS_ALLOWED_ORIGINS` ;
- suppression unitaire et multiple limitée au propriétaire des fichiers ;
- validation des uploads avec contrôle du type MIME, de la taille et du fichier vide ;
- protection contre le path traversal sur les noms de fichiers ;
- réponses d’erreur homogènes via un gestionnaire global d’exceptions ;
- expiration des liens de téléchargement après 7 jours ;
- purge planifiée des fichiers expirés pour maintenir la cohérence entre base et stockage local ;
- scan OWASP Dependency Check versionné dans `docs/security`.

## Tests

Lancer les tests :

```bash
./mvnw test
```

Le projet utilise :

- un profil test dédié ;
- une base H2 en mémoire pour les tests ;
- une configuration distincte pour éviter de dépendre de PostgreSQL local;
- JaCoCo pour la couverture ;
- des tests unitaires et d’intégration backend.

Les tests couvrent notamment :

- l’authentification ;
- la génération et validation JWT ;
- les accès protégés ;
- les JWT invalides ou malformés ;
- l’upload de fichiers ;
- la validation des fichiers ;
- l’historique ;
- la suppression unitaire ;
- la suppression multiple ;
- le téléchargement public ;
- la purge des fichiers expirés ;
- la gestion globale des erreurs.

## État actuel

- tests backend au vert ;
- couverture JaCoCo backend supérieure au seuil minimal visé ;
- documentation OpenAPI disponible ;
- migrations Flyway actives ;
- scan OWASP Dependency Check versionné ;
- purge planifiée des fichiers expirés active.

Le rapport JaCoCo est généré dans :

```bash
target/site/jacoco/index.html
```

Le rapport de sécurité des dépendances est généré dans :

```text
docs/security/dependency-check/dependency-check-report.html
```

## Qualité et sécurité des dépendances

Générer l’arbre des dépendances Maven :

```bash
./mvnw dependency:tree | grep -v "Progress" > docs/security/maven-dependency-tree.txt
```

Lancer le scan OWASP Dependency Check :

```bash
./mvnw dependency-check:check
```

Les rapports sont générés dans :

```text
docs/security/dependency-check/
```

Le scan permet d’identifier les vulnérabilités connues sur les dépendances directes et transitives du backend.

Les résultats doivent être analysés selon :

- la criticité ;
- l’exposition réelle dans l’application ;
- le caractère direct ou transitif de la dépendance ;
- le risque de régression en cas de mise à jour immédiate.

Les décisions de sécurité sont documentées dans `SECURITY.md`.

## Structure du projet

```bash
src/main/java/com/datashare/
  configuration/
    security/
  controller/
  dto/
    auth/
    common/
    file/
  entities/
  exception/
  repository/
  service/

src/main/resources/
  db/
    migration/
  application.yml
  application-test.yml

docs/
  security/
```

## Limites et évolutions identifiées

Les principales évolutions prévues avant un usage production sont :

- ajout d’une pagination sur `GET /api/files` ;
- ajout d’un rate limiting sur `POST /api/auth/login` ;
- durcissement de la stratégie de stockage du JWT côté frontend ;
- désactivation ou protection de Swagger UI hors environnement de développement ;
- automatisation de la veille dépendances avec Dependabot, Renovate ou une étape CI ;
- amélioration de l’observabilité avec logs structurés, traceId et métriques applicatives ;
- protection optionnelle des liens de téléchargement par mot de passe, correspondant à une évolution fonctionnelle optionnelle.
