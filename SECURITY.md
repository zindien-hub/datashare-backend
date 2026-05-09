# SECURITY.md

## Objectif

Ce document décrit les principales mesures de sécurité mises en place sur le backend DataShare.

L’objectif est de sécuriser les fonctions critiques du prototype :

- authentification ;
- gestion des accès ;
- protection des fichiers ;
- manipulation des secrets de configuration.

## Mesures de sécurité mises en place

Le backend met en place les mécanismes suivants :

- authentification par JWT ;
- hachage des mots de passe avec BCrypt ;
- séparation entre routes publiques et routes protégées ;
- contrôle de propriété pour la suppression des fichiers ;
- externalisation de la configuration sensible via variables d’environnement.

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

## Gestion des accès

Les routes sont séparées entre accès public et accès authentifié.

### Routes publiques

- `/api/auth/**`
- `/download/**`

### Routes protégées

- `/api/files/**`

L’accès aux fonctionnalités d’upload, d’historique et de suppression nécessite un JWT valide.

## Protection des mots de passe

Les mots de passe ne sont jamais stockés en clair.

Ils sont :

- hachés avec BCrypt avant enregistrement ;
- vérifiés via le `PasswordEncoder` au moment de la connexion.

Cette approche permet d’éviter le stockage direct des secrets utilisateur en base.

## Sécurité des fichiers

### Upload
Les fichiers sont associés à l’utilisateur authentifié qui les a envoyés.

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

Le fichier `.env` est ignoré par Git.

## Vérifications réalisées

Les vérifications suivantes ont été réalisées pendant le développement :

- validation de la création d’utilisateur avec mot de passe hashé en base ;
- validation de la génération d’un JWT à la connexion ;
- validation de l’accès protégé aux routes `/api/files/**` ;
- validation du téléchargement public par token ;
- validation du contrôle de propriété sur la suppression.

## Limites actuelles

À ce stade du prototype, certaines mesures de sécurité peuvent encore être renforcées :

- absence de rotation automatique des secrets ;
- absence de gestion de rôles avancée ;
- absence de limitation de débit sur les endpoints sensibles ;
- absence de protection supplémentaire par mot de passe sur les fichiers partagés ;
- absence de journalisation de sécurité avancée.

## Améliorations prévues

Les améliorations envisagées sont :

- ajouter des tests automatisés de sécurité sur les endpoints critiques ;
- renforcer la validation des fichiers envoyés ;
- mettre en place des limites de taille et des règles de type MIME plus strictes ;
- ajouter une protection optionnelle par mot de passe pour les liens de téléchargement ;
- compléter le durcissement global pour un environnement de production.
