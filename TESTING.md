# TESTING.md

## Objectif

Ce document décrit l’état actuel des vérifications réalisées sur le backend Spring Boot de DataShare.

L’objectif est de sécuriser les fonctionnalités critiques du service :

- authentification ;
- validation des JWT ;
- upload de fichier ;
- validation des fichiers envoyés ;
- historique des fichiers ;
- téléchargement public ;
- suppression unitaire et multiple ;
- sécurité des accès ;
- gestion des erreurs ;
- purge des fichiers expirés.

## Stratégie retenue

La validation du backend repose sur trois approches complémentaires :

- des tests unitaires sur les services et composants critiques ;
- des tests d’intégration sur les contrôleurs et la configuration de sécurité ;
- des tests manuels sur les parcours métier principaux ;
- une vérification de couverture avec JaCoCo.

Cette stratégie vise à sécuriser les parcours critiques du MVP : inscription, connexion, upload, historique, téléchargement public, suppression et gestion des erreurs.

Les tests automatisés sont priorisés sur les zones portant le comportement applicatif : services, contrôleurs, sécurité, exceptions et jobs planifiés.

## Outils

- Maven
- Spring Boot Test
- Mockito
- Spring Security Test
- MockMvc
- JaCoCo
- profil `test`
- base H2 en mémoire
- OWASP Dependency Check pour le scan de sécurité des dépendances

## Exécution des tests

Lancer les tests backend :

```bash
./mvnw test
```

Le rapport de couverture JaCoCo est ensuite généré dans :

```text
target/site/jacoco/index.html
```

Lancer le scan de sécurité des dépendances :

```bash
./mvnw dependency-check:check
```

Les rapports de sécurité sont générés dans :

```text
docs/security/dependency-check/
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

Des tests d’intégration valident la configuration de sécurité et le comportement des endpoints :

- accès public aux endpoints `/api/auth/register` et `/api/auth/login` ;
- protection des endpoints `/api/files/**` ;
- accès public à `/download/{token}` ;
- accès autorisé aux endpoints protégés avec utilisateur authentifié ;
- rejet des routes protégées sans authentification avec un statut `401` ;
- rejet des JWT invalides ou malformés avec un statut `401` ;
- validation de la distinction entre utilisateur non authentifié et accès non autorisé.

## Résultat actuel

La commande suivante passe avec succès :

```bash
./mvnw test
```

Résultat observé :

- **49 tests backend au vert**
- **0 échec**
- **0 erreur**
- **0 test ignoré**

### Couverture JaCoCo

Le backend atteint actuellement :

- **79 % de couverture d’instructions**
- **67 % de couverture de branches**

Cela permet de dépasser le seuil minimal visé de **70 % sur les instructions**.

### Scan de sécurité

Le scan OWASP Dependency Check est également exécuté afin de produire un rapport de sécurité versionné.

Commande utilisée :

```bash
./mvnw dependency-check:check
```

Les rapports sont disponibles dans :

```text
docs/security/dependency-check/
```

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

- certains chemins secondaires des services métier ;
- certains scénarios conditionnels de sécurité ;
- les cas limites liés à la configuration ;
- les entités JPA, qui contiennent peu de logique métier.

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

- appel à `POST /api/auth/register` ;
- création correcte de l’utilisateur en base PostgreSQL ;
- stockage du mot de passe hashé avec BCrypt.

### 2. Connexion

- appel à `POST /api/auth/login` ;
- retour d’un JWT valide ;
- utilisation du JWT dans l’en-tête `Authorization: Bearer <token>`.

### 3. Sécurité des routes protégées

- appel à `GET /api/files` sans JWT ;
- vérification du retour `401 Unauthorized` ;
- appel à `GET /api/files` avec JWT invalide ;
- vérification du retour `401 Unauthorized`.

### 4. Upload authentifié

- appel à `POST /api/files` avec JWT ;
- création du fichier dans le répertoire local configuré par `UPLOAD_DIR` ;
- création de la ligne correspondante dans la table `shared_files` ;
- vérification du retour du champ `size` dans la réponse d’upload.

### 5. Historique des fichiers

- appel à `GET /api/files` avec JWT ;
- récupération des fichiers du propriétaire connecté ;
- tri du plus récent au plus ancien ;
- vérification des métadonnées retournées : nom, type, taille, token, URL, date d’expiration.

### 6. Téléchargement public

- appel à `GET /download/{token}` ;
- récupération correcte du fichier via son token public ;
- restitution du nom de fichier et du type MIME attendus ;
- rejet d’un token inconnu ou expiré.

### 7. Suppression

- appel à `DELETE /api/files/{id}` avec JWT ;
- suppression de l’entrée en base ;
- suppression du fichier dans le répertoire local ;
- réponse `204 No Content`.

### 8. Suppression multiple

- sélection de plusieurs fichiers ;
- appel à `POST /api/files/bulk-delete` avec JWT ;
- suppression des entrées en base ;
- suppression des fichiers physiques ;
- réponse `204 No Content`.

### 9. Validation d’upload

- rejet d’un fichier vide ;
- rejet d’un fichier trop volumineux ;
- rejet d’un type de fichier non autorisé ;
- rejet d’un nom de fichier contenant une tentative de path traversal ;
- retour d’une réponse JSON homogène en cas d’erreur.

### 10. Purge des fichiers expirés

- création d’un fichier expiré en base ;
- exécution du job planifié de purge ;
- suppression vérifiée en base PostgreSQL ;
- suppression vérifiée dans le répertoire local d’upload.

## Vérifications techniques complémentaires

Des vérifications complémentaires ont été réalisées pendant le développement :

- inspection des données dans PostgreSQL après inscription, upload et suppression ;
- vérification de la création automatique du dossier d’upload ;
- vérification de la cohérence entre stockage local et données persistées ;
- vérification de la forme homogène des réponses d’erreur JSON après ajout du gestionnaire global d’exceptions ;
- vérification du comportement CORS en local avec le frontend Angular ;
- vérification de la désactivation de l’authentification HTTP Basic ;
- génération du rapport JaCoCo ;
- génération du rapport OWASP Dependency Check.

## Limites actuelles

Malgré la progression de la couverture, plusieurs points restent perfectibles :

- la couverture de branches reste inférieure au niveau cible ;
- certains scénarios conditionnels peuvent encore être renforcés ;
- certains cas limites métier peuvent être davantage testés ;
- les entités JPA ne sont pas directement couvertes, car elles contiennent peu de logique métier ;
- les tests de charge et de performance restent documentés séparément dans `PERF.md` ;
- la pagination de l’historique n’est pas encore implémentée ;
- le rate limiting de l’endpoint de connexion reste à ajouter.

## Améliorations prévues

Les améliorations envisagées sont :

- renforcer la couverture de branches ;
- ajouter davantage de tests sur les cas d’erreur de sécurité ;
- compléter les tests d’intégration sur les endpoints REST ;
- tester plus finement les scénarios limites dans les services métier ;
- ajouter des tests autour de la future pagination de l’historique ;
- ajouter des tests de rate limiting lorsque la fonctionnalité sera mise en place ;
- poursuivre l’amélioration de la robustesse globale du backend.
