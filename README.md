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
- PostgreSQL
- Docker Compose
- Lombok
- springdoc-openapi

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
```

Créer ensuite un fichier `.env` local à partir de ce modèle.

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

- `POST /api/auth/register`
- `POST /api/auth/login`

### Fichiers authentifiés

- `POST /api/files` : upload d’un fichier
- `GET /api/files` : historique des fichiers de l’utilisateur connecté
- `DELETE /api/files/{id}` : suppression d’un fichier appartenant à l’utilisateur connecté
- `POST /api/files/bulk-delete` : suppression multiple de fichiers appartenant à l’utilisateur connecté

### Téléchargement public

- `GET /download/{token}` : téléchargement public d’un fichier via son token

## Documentation API

La documentation OpenAPI est générée dynamiquement via springdoc-openapi.

Elle est accessible localement via :

- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`
- `http://localhost:8080/v3/api-docs.yaml`

Un fichier documentaire complémentaire peut également être conservé dans :

- `docs/openapi.yaml`

## Documentation complémentaire

Les documents complémentaires du projet sont disponibles dans `docs/`, notamment :

- `docs/TECHNICAL-DOCUMENTATION.md`
- `docs/AI-USAGE.md`
- `docs/openapi.yaml`

## Sécurité

- authentification par JWT ;
- mots de passe stockés hashés avec BCrypt ;
- routes `/api/auth/**` et `/download/**` publiques ;
- routes `/api/files/**` protégées ;
- suppression unitaire et multiple limitée au propriétaire des fichiers ;
- validation des uploads avec contrôle du type et de la taille ;
- réponses d’erreur homogènes via un gestionnaire global d’exceptions ;
- expiration des liens de téléchargement après 7 jours ;
- purge planifiée des fichiers expirés pour maintenir la cohérence entre base et stockage local.

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

## État actuel

- tests backend au vert ;
- couverture JaCoCo backend supérieure au seuil minimal visé sur les instructions.

Le rapport JaCoCo est généré dans :
```bash
target/site/jacoco/index.html
```

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
```
