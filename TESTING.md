# TESTING.md

## Objectif

Ce document décrit l’état actuel des vérifications réalisées sur le backend Spring Boot de DataShare.

L’objectif est de sécuriser les fonctionnalités critiques du service :

- authentification ;
- upload de fichier ;
- historique des fichiers ;
- téléchargement public ;
- suppression d’un fichier.

## Stratégie retenue

À ce stade du projet, la validation du backend repose sur deux approches complémentaires :

- des tests automatisés de contexte et de configuration ;
- des tests fonctionnels manuels sur les endpoints critiques.

Cette stratégie a permis de sécuriser le MVP tout en mettant en place une base de tests automatisés compatible avec l’architecture actuelle.

## Outils

- Maven
- Spring Boot Test
- profil `test`
- base H2 en mémoire

## Exécution des tests

Lancer les tests backend :

```bash
./mvnw test
```

## Tests automatisés actuellement en place

### Couverture actuelle

À ce stade, le backend dispose d’un test automatisé principal :

- chargement du contexte Spring (`contextLoads`) ;
- validation du profil `test` ;
- utilisation d’une base H2 en mémoire pour éviter une dépendance à PostgreSQL local.

## Résultat actuel

Le backend dispose actuellement d’un socle minimal de test automatisé.

La commande suivante passe avec succès :

```bash
./mvnw test
INFO] Scanning for projects...
[INFO] 
[INFO] ------------------< com.datashare:datashare-backend >-------------------
[INFO] Building  0.0.1-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
...
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.datashare.DatashareBackendApplicationTests
...
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.224 s -- in com.datashare.DatashareBackendApplicationTests
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  5.894 s
[INFO] Finished at: 2026-05-08T23:07:30+02:00
[INFO] ------------------------------------------------------------------------
```

## Vérifications manuelles réalisées

Les scénarios suivants ont été validés manuellement sur le backend :

### 1. Inscription

- appel à `POST /api/auth/register` ;
- création correcte de l’utilisateur en base PostgreSQL ;
- stockage du mot de passe hashé avec BCrypt.

### 2. Connexion

- appel à `POST /api/auth/login` ;
- retour d’un JWT valide.

### 3. Upload authentifié

- appel à `POST /api/files` avec JWT ;
- création du fichier dans le répertoire local configuré par `UPLOAD_DIR` ;
- création de la ligne correspondante dans la table shared_files.

### 4. Historique des fichiers

- appel à `GET /api/files` avec JWT ;
- récupération des fichiers du propriétaire connecté ;
- tri du plus récent au plus ancien.

### 5. Téléchargement public

- appel à `GET /download/{token}` ;
- récupération correcte du fichier via son token public ;
- restitution du nom de fichier et du type MIME attendus.

### 6. Suppression

- appel à `DELETE /api/files/{id}` avec JWT ;
- suppression de l’entrée en base ;
- suppression du fichier dans le répertoire local ;
- réponse 204 No Content.

## Vérifications techniques complémentaires

Des vérifications complémentaires ont aussi été réalisées pendant le développement :

- inspection des données dans PostgreSQL après inscription, upload et suppression ;
- vérification de la création automatique du dossier d’uploads ;
- vérification de la cohérence entre stockage local et données persistées.

## Limites actuelles

Le backend ne dispose pas encore de tests automatisés métier sur les services et controllers.

À ce stade, la validation repose principalement sur :

- un test automatisé de chargement du contexte ;
- des vérifications manuelles sur les endpoints critiques.

En particulier, il reste à renforcer la couverture sur :

- `JwtService` ;
- `FileService` ;
- les controllers REST ;
- les cas d’erreur métier et de sécurité.

## Améliorations prévues

Les améliorations envisagées sont :

- ajouter des tests unitaires sur les services métier ;
- ajouter des tests d’intégration sur les principaux endpoints ;
- tester davantage les cas d’erreur ;
- produire un rapport de couverture consolidé.
