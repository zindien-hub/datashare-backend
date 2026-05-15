# TESTING.md

## Objectif

Ce document décrit l’état actuel des vérifications réalisées sur le backend Spring Boot de DataShare.

L’objectif est de sécuriser les fonctionnalités critiques du service :

- authentification ;
- upload de fichier ;
- historique des fichiers ;
- téléchargement public ;
- suppression d’un fichier ;
- sécurité des accès ;
- gestion des erreurs.

## Stratégie retenue

La validation du backend repose sur trois approches complémentaires :

- des tests unitaires sur les services et composants critiques ;
- des tests d’intégration sur les contrôleurs et la sécurité ;
- des tests manuels sur les parcours métier principaux.

Cette stratégie a permis de renforcer la fiabilité du backend tout en améliorant la couverture de code et la vérification des comportements critiques.

## Outils

- Maven
- Spring Boot Test
- Mockito
- Spring Security Test
- MockMvc
- JaCoCo
- profil `test`
- base H2 en mémoire

## Exécution des tests

Lancer les tests backend :

```bash
./mvnw test
```

Le rapport de couverture JaCoCo est ensuite généré dans :

```
target/site/jacoco/index.html
```

## Tests automatisés actuellement en place

Les tests backend couvrent désormais plusieurs niveaux.

### Tests unitaires

Les composants suivants sont couverts par des tests unitaires :

- `JwtService`
- `FileService`
- `ExpiredFileCleanupService`
- `GlobalExceptionHandler`
- `AuthController`
- `FileController`
- `PublicFileController`

### Tests d’intégration / sécurité

Des tests d’intégration ont été ajoutés pour valider la configuration de sécurité et le comportement des endpoints :

- accès public aux endpoints `/api/auth/register` et `/api/auth/login`
- protection des endpoints `/api/files/**`
- accès public à `/download/{token}`
- accès autorisé aux endpoints protégés avec utilisateur authentifié

## Résultat actuel

La commande suivante passe avec succès :

```bash
./mvnw test
```

Résultat observé :

- **42 tests backend au vert**
- **0 échec**
- **0 erreur**
- **0 test ignoré**

### Couverture JaCoCo

Le backend atteint actuellement :

- **76 % de couverture d’instructions**
- **58 % de couverture de branches**

Cela permet de dépasser le seuil minimal visé de **70 % sur les instructions**.

## Analyse de la couverture

### Zones bien couvertes

Les packages suivants présentent une très bonne couverture :

- `com.datashare.controller`
- `com.datashare.exception`
- `com.datashare.dto.file`
- `com.datashare.dto.auth`
- `com.datashare.dto.common`
- `com.datashare.configuration`

### Zones encore à renforcer

Les principales marges de progression se situent sur :

- `com.datashare.service`
- `com.datashare.configuration.security`
- `com.datashare.entities`

La couverture de branches reste perfectible, ce qui montre que tous les scénarios conditionnels ne sont pas encore exercés.

### Cas particulier des entités

Le package `com.datashare.entities` reste peu couvert car il contient principalement des entités JPA sans logique métier complexe.

L’effort de test a été concentré en priorité sur :

- les services ;
- les contrôleurs ;
- la sécurité ;
- la gestion des erreurs ;

car ce sont eux qui portent le comportement applicatif critique.

## Vérifications manuelles réalisées

Les scénarios suivants ont été validés manuellement sur le backend :

### 1. Inscription
    - appel à POST `/api/auth/register`
    - création correcte de l’utilisateur en base PostgreSQL
    - stockage du mot de passe hashé avec BCrypt

### 2. Connexion
    - appel à POST `/api/auth/login`
    - retour d’un JWT valide

### 3. Upload authentifié
    - appel à `POST /api/files` avec JWT
    - création du fichier dans le répertoire local configuré par `UPLOAD_DIR`
    - création de la ligne correspondante dans la table `shared_files`

### 4. Historique des fichiers
    - appel à `GET /api/files` avec JWT
    - récupération des fichiers du propriétaire connecté
    - tri du plus récent au plus ancien

### 5. Téléchargement public
    - appel à `GET /download/{token}`
    - récupération correcte du fichier via son token public
    - restitution du nom de fichier et du type MIME attendus

### 6. Suppression
    - appel à `DELETE /api/files/{id}` avec JWT
    - suppression de l’entrée en base
    - suppression du fichier dans le répertoire local
    - réponse `204 No Content`

### 7. Validation d’upload
    - rejet d’un fichier vide
    - rejet d’un type de fichier non autorisé
    - retour d’une réponse JSON homogène en cas d’erreur

### 8. Purge des fichiers expirés
- création d’un fichier expiré en base ;
- exécution du job planifié de purge ;
- suppression vérifiée en base PostgreSQL ;
- suppression vérifiée dans le répertoire local d’upload.

## Vérifications techniques complémentaires

Des vérifications complémentaires ont été réalisées pendant le développement :

- inspection des données dans PostgreSQL après inscription, upload et suppression
- vérification de la création automatique du dossier d’upload
- vérification de la cohérence entre stockage local et données persistées
- vérification de la forme homogène des réponses d’erreur JSON après ajout du gestionnaire global d’exceptions

## Limites actuelles

Malgré la progression de la couverture, plusieurs points restent perfectibles :

- la couverture de branches reste inférieure au niveau cible
- le package `configuration.security` peut être davantage testé sur les scénarios conditionnels
- le package `service` peut encore être renforcé sur certains cas d’erreur ou chemins secondaires
- les entités JPA ne sont pas directement couvertes
- tous les cas d’erreur métier ne sont pas encore testés en profondeur

## Améliorations prévues

Les améliorations envisagées sont :

- renforcer la couverture de branches
- ajouter davantage de tests sur les cas d’erreur de sécurité
- compléter les tests d’intégration sur les endpoints REST
- tester plus finement les scénarios limites dans les services métier
- poursuivre l’amélioration de la robustesse globale du backend
