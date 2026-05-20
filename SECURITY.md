# SECURITY.md

## Objectif

Ce document décrit les principales mesures de sécurité mises en place sur le backend DataShare.

L’objectif est de sécuriser les fonctions critiques du prototype :

- authentification ;
- gestion des accès ;
- protection des fichiers ;
- validation des entrées ;
- manipulation des secrets de configuration ;
- suivi des dépendances et des vulnérabilités connues.

## Mesures de sécurité mises en place

Le backend met en place les mécanismes suivants :

- authentification par JWT ;
- hachage des mots de passe avec BCrypt ;
- séparation entre routes publiques et routes protégées ;
- contrôle de propriété pour la suppression des fichiers ;
- validation serveur des fichiers envoyés ;
- protection contre le path traversal ;
- gestion homogène des erreurs HTTP ;
- configuration CORS explicite ;
- externalisation de la configuration sensible via variables d’environnement ;
- purge planifiée des fichiers expirés ;
- scan de sécurité des dépendances backend.

## Authentification

L’authentification repose sur un token JWT généré lors de la connexion utilisateur.

### Endpoints concernés

- `POST /api/auth/register`
- `POST /api/auth/login`

### Fonctionnement

- l’utilisateur se connecte avec son email et son mot de passe ;
- le backend valide les identifiants ;
- un JWT est généré et renvoyé au frontend ;
- le token est ensuite transmis dans l’en-tête `Authorization: Bearer <token>` pour les routes protégées.

### Durcissement JWT

Le filtre JWT gère les tokens invalides, expirés ou malformés sans provoquer d’erreur serveur.

En cas de token absent ou invalide :

- l’utilisateur n’est pas authentifié ;
- les routes protégées répondent avec un statut `401 Unauthorized` ;
- le backend évite de remonter une exception technique en `500 Internal Server Error`.

L’authentification HTTP Basic et le formulaire de login Spring Security sont désactivés, car l’application repose sur une authentification JWT stateless.

## Gestion des accès

Les routes sont séparées entre accès public et accès authentifié.

### Routes publiques

- `/api/auth/**`
- `/download/**`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/swagger-ui.html`

### Routes protégées

- `/api/files/**`

L’accès aux fonctionnalités d’upload, d’historique et de suppression nécessite un JWT valide.

Le backend distingue les cas suivants :

- `401 Unauthorized` : l’utilisateur n’est pas authentifié ou le token est invalide ;
- `403 Forbidden` : l’utilisateur est authentifié mais tente d’accéder à une ressource qui ne lui appartient pas.

## Protection des mots de passe

Les mots de passe ne sont jamais stockés en clair.

Ils sont :

- hachés avec BCrypt avant enregistrement ;
- vérifiés via le `PasswordEncoder` au moment de la connexion.

Cette approche permet d’éviter le stockage direct des secrets utilisateur en base.

## Sécurité des fichiers

### Upload

Les fichiers sont associés à l’utilisateur authentifié qui les a envoyés.

Le backend applique plusieurs contrôles côté serveur :

- rejet des fichiers vides ;
- contrôle de la taille maximale autorisée ;
- contrôle du type MIME ;
- nettoyage du nom original du fichier ;
- stockage physique sous un nom technique basé sur un UUID ;
- vérification que le chemin final reste dans le répertoire d’upload autorisé.

Ces contrôles permettent de limiter les risques liés aux fichiers non conformes et au path traversal.

### Suppression
La suppression d’un fichier n’est autorisée que pour son propriétaire.

Le backend vérifie explicitement que :

- le fichier existe ;
- l’utilisateur authentifié correspond bien au propriétaire du fichier.

### Téléchargement public
Le téléchargement public repose sur un token unique stocké avec les métadonnées du fichier.

Le backend vérifie :

- l’existence du token ;
- la disponibilité du fichier ;
- l’expiration du lien avant de servir le contenu.

Les liens expirent après 7 jours.
Un job planifié supprime ensuite les fichiers expirés du disque et de la base afin de limiter l’accumulation de données obsolètes.

### Protection par mot de passe des fichiers

La protection optionnelle par mot de passe des fichiers correspond à l’US09 du cadrage fonctionnel.

Cette fonctionnalité n’est pas livrée dans le périmètre MVP actuel. Le champ technique prévu en base devra être soit retiré par migration si l’US09 n’est pas retenue, soit utilisé dans une implémentation complète avec :

- saisie d’un mot de passe à l’upload ;
- hachage du mot de passe fichier ;
- vérification du mot de passe avant téléchargement ;
- gestion des erreurs côté frontend et backend.

## CORS

Le backend définit une configuration CORS explicite afin de ne pas dépendre uniquement du proxy Angular utilisé en développement.

Les origines autorisées sont externalisées via la propriété :

```yaml
app:
  cors:
    allowed-origins: ${APP_CORS_ALLOWED_ORIGINS:http://localhost:4200}
```

En local, l’origine autorisée est généralement :

```yaml
APP_CORS_ALLOWED_ORIGINS=http://localhost:4200
```

En production, cette valeur devra être remplacée par le domaine réel du frontend, par exemple :

```yaml
APP_CORS_ALLOWED_ORIGINS=https://datashare.example.com
```

Cette configuration permet d’adapter le déploiement sans modifier le code Java.

## Configuration et secrets

Les paramètres sensibles ne sont pas codés en dur dans le projet.

Ils sont externalisés via le fichier `.env` local et documentés dans `.env.example`, notamment :

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`
- `UPLOAD_DIR`
- `APP_CORS_ALLOWED_ORIGINS`

Le fichier `.env` est ignoré par Git.

## Scan de sécurité des dépendances backend

Un scan OWASP Dependency Check a été exécuté sur le backend afin d’identifier les vulnérabilités connues dans les dépendances Maven.

### Commande exécutée

```bash
./mvnw dependency-check:check
```

Le plugin génère les rapports dans :

```bash
docs/security/dependency-check/
```

Fichiers principaux :

```bash
dependency-check-report.html
dependency-check-report.json
```

Un export de l’arbre des dépendances Maven peut également être généré avec :

```bash
./mvnw dependency:tree > docs/security/maven-dependency-tree.txt
```

### Remarque sur l’analyseur OSS Index

Lors de l’exécution du scan, l’analyseur Sonatype OSS Index peut retourner une erreur `401 Unauthorized` sans authentification dédiée.

Pour éviter que ce service externe bloque la génération du rapport, l’analyseur OSS Index est désactivé dans la configuration du plugin OWASP Dependency Check.

Le scan OWASP Dependency Check reste exécuté et les rapports générés sont conservés dans le dépôt.

### Résultat du scan

Le scan a analysé les dépendances du backend et a identifié plusieurs vulnérabilités connues sur des dépendances directes ou transitives.

Les principales dépendances signalées sont :

| Dépendance                 | Analyse                                                                                       | Décision                                                                                        |
| -------------------------- | --------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| `tomcat-embed-core`        | Dépendance embarquée par Spring Boot, exposée car elle porte le serveur HTTP de l’application | Mise à jour Spring Boot / Tomcat à planifier avant production                                   |
| `postgresql`               | Driver JDBC utilisé par le backend pour communiquer avec PostgreSQL                           | Correction par montée de version du driver Maven                                                |
| `swagger-ui` / `DOMPurify` | Dépendance liée à l’interface Swagger UI de documentation API                                 | Risque limité au périmètre documentation ; à mettre à jour ou désactiver/protéger en production |
| `hibernate-validator`      | Dépendance de validation utilisée via l’écosystème Spring Boot                                | À suivre via la mise à jour des dépendances managées Spring Boot                                |
| `angus-activation`         | Dépendance transitive Jakarta Activation                                                      | Risque d’exposition faible dans le périmètre actuel ; à suivre lors des mises à jour            |

### Cas du driver PostgreSQL

La vulnérabilité détectée sur `postgresql-42.7.10.jar` concerne le driver JDBC utilisé par le backend Java pour communiquer avec PostgreSQL.

Elle ne relève pas d’une migration Flyway, car Flyway gère le schéma de base de données, tandis que le driver JDBC est une dépendance Maven.

La correction consiste donc à mettre à jour la dépendance `org.postgresql:postgresql` dans le `pom.xml`, puis à relancer les tests et le scan de sécurité.

### Décisions de traitement

Pour le périmètre MVP, le scan est utilisé comme outil d’analyse et de suivi sécurité.

Les vulnérabilités ne sont pas ignorées : elles sont classées selon :

- leur criticité ;
- leur exposition réelle dans l’application ;
- leur présence en dépendance directe ou transitive ;
- le risque de régression en cas de mise à jour immédiate.

Actions retenues :

- corriger les dépendances simples à mettre à jour sans impact fonctionnel ;
- planifier une montée de version Spring Boot / Tomcat ;
- désactiver ou protéger Swagger UI en production ;
- relancer le scan après chaque mise à jour majeure ;
- automatiser ce contrôle via Dependabot, Renovate ou une étape CI.

## Vérifications réalisées

Les vérifications suivantes ont été réalisées pendant le développement :

- validation de la création d’utilisateur avec mot de passe hashé en base ;
- validation de la génération d’un JWT à la connexion ;
- validation du retour HTTP 401 en cas d’identifiants invalides ;
- validation du retour HTTP 401 en cas de JWT absent ou invalide ;
- validation du retour HTTP 403 en cas d’accès à une ressource non autorisée ;
- validation de l’accès protégé aux routes `/api/files/**` ;
- validation du téléchargement public par token ;
- validation de l’expiration des liens de téléchargement ;
- validation du contrôle de propriété sur la suppression ;
- validation du rejet des fichiers vides ;
- validation du rejet des fichiers trop volumineux ;
- validation du rejet des types MIME non autorisés ;
- validation de la protection contre le path traversal ;
- validation de la purge planifiée des fichiers expirés ;
- génération d’un rapport OWASP Dependency Check.

## Limites actuelles

À ce stade du prototype, certaines mesures de sécurité peuvent encore être renforcées :

- absence de rotation automatique des secrets ;
- absence de gestion de rôles avancée ;
- absence de limitation de débit sur les endpoints sensibles, notamment `POST /api/auth/login` ;
- stockage du JWT côté frontend en `localStorage`, solution simple pour le MVP mais moins robuste qu’un cookie `HttpOnly`, `Secure`, `SameSite` ;
- absence de protection supplémentaire par mot de passe sur les fichiers partagés ;
- pagination absente sur `GET /api/files`, ce qui peut devenir un risque de performance ou d’abus si le volume augmente ;
- Swagger UI accessible en environnement local/démonstration, à désactiver ou protéger en production ;
- observabilité encore perfectible : les logs applicatifs ont été renforcés, mais il n’y a pas encore de journalisation structurée complète avec corrélation avancée de traces.

## Améliorations prévues

Les améliorations envisagées sont :

- ajouter un rate limiting sur `POST /api/auth/login` afin de limiter les tentatives de brute force ;
- basculer la gestion du JWT vers un cookie `HttpOnly`, `Secure`, `SameSite` pour un usage production ;
- ajouter une protection optionnelle par mot de passe pour les liens de téléchargement ;
- mettre en place une pagination sur l’historique des fichiers ;
- compléter le durcissement CORS avec les domaines de production réels ;
- désactiver ou protéger Swagger UI hors environnement de développement ;
- automatiser les scans de dépendances avec Dependabot, Renovate ou une étape CI ;
- définir un seuil bloquant de vulnérabilités en CI pour un environnement de production ;
- compléter l’observabilité avec des logs structurés, un identifiant de corrélation et des métriques applicatives ;
- compléter le durcissement global pour un environnement de production.
