# MAINTENANCE.md

## Objectif

Ce document décrit les éléments de maintenance du backend Spring Boot de DataShare.

L’objectif est de faciliter :
- la compréhension du service ;
- les corrections de bugs ;
- les évolutions fonctionnelles ;
- la stabilité du backend dans le temps.

## Organisation du projet

Le backend est structuré de manière à séparer clairement :
- la configuration ;
- les controllers REST ;
- les DTO ;
- les entités ;
- les repositories ;
- les services métier.

Structure principale :

```text
src/main/java/com/datashare/
  configuration/security/
  controller/
  dto/
    auth/
    file/
  entities/
  repository/
  service/
```

Cette organisation facilite l’identification des responsabilités et limite le couplage entre couches.

## Principes de maintenance retenus

Les principes suivants ont été appliqués :

- séparation entre couche web, couche métier et couche accès aux données ;
- centralisation de la logique métier dans les services ;
- centralisation de la configuration de sécurité ;
- externalisation de la configuration sensible via variables d’environnement ;
- documentation progressive des fonctionnalités et des choix effectués.

## Zones sensibles du backend

Les parties les plus sensibles du backend sont :

### Authentification et sécurité

- génération et validation des JWT ;
- filtrage des requêtes protégées ;
- séparation entre routes publiques et routes authentifiées.

### Gestion des fichiers

- écriture et suppression de fichiers sur disque ;
- cohérence entre stockage local et base de données ;
- vérification de propriété avant suppression ;
- gestion des liens publics de téléchargement.

### Configuration

- cohérence entre `.env`, `application.yml` et profil `test` ;
- gestion de la base PostgreSQL en local ;
- gestion du répertoire d’upload.

## Dette technique actuelle

À ce stade du prototype, plusieurs points peuvent être renforcés :

- la couverture de tests backend reste limitée ;
- les services métier ne disposent pas encore de tests automatisés détaillés ;
- les controllers REST peuvent être davantage testés en intégration ;
- la gestion des erreurs peut être mieux standardisée ;
- certaines validations métier peuvent être enrichies.

## Maintenance corrective

En cas de bug backend, la démarche recommandée est :

1. reproduire le problème sur l’endpoint ou le service concerné ;
2. identifier s’il s’agit d’un problème :
    - de sécurité ;
    - de logique métier ;
    - de persistance ;
    - de stockage local ;
    - de configuration ;
3. corriger dans la couche appropriée ;
4. valider par test manuel et/ou automatisé ;
5. documenter si le changement affecte le comportement attendu.

## Maintenance évolutive

Les évolutions suivantes sont prévues ou envisageables :

- ajout de tests automatisés sur `JwtService` et `FileService` ;
- renforcement des validations sur les uploads ;
- amélioration de la gestion d’erreurs ;
- ajout éventuel d’une protection par mot de passe sur les fichiers ;
- ajout de règles plus fines sur l’expiration ou l’accès aux fichiers partagés.

## Dépendances et mises à jour

Le backend repose principalement sur :

- Spring Boot ;
- Spring Security ;
- Spring Data JPA ;
- PostgreSQL ;
- Lombok.

La maintenance doit inclure :

- la surveillance des dépendances Maven ;
- la mise à jour régulière des bibliothèques ;
- la vérification de compatibilité après mise à jour ;
- l’exécution des tests après changement de version.

## Données et stockage local

Le backend manipule deux types de persistance :

- la base PostgreSQL pour les utilisateurs et métadonnées ;
- le stockage local pour les fichiers uploadés.

Une attention particulière doit être portée à :

- la cohérence entre base et système de fichiers ;
- le nettoyage des fichiers orphelins ;
- la gestion du dossier `uploads/` non versionné ;
- la reproductibilité du setup local via `.env` et `compose.yaml`.

## Bonnes pratiques recommandées

Pour maintenir le backend dans de bonnes conditions :

- conserver une séparation nette entre services et controllers ;
- éviter d’introduire de logique métier dans les controllers ;
- centraliser les règles de sécurité ;
- renforcer progressivement la couverture de tests ;
- documenter les changements importants dans les fichiers de suivi ;
- conserver une gestion claire des variables d’environnement.

## Conclusion

Le backend DataShare dispose d’une structure simple et maintenable pour un MVP.

Les priorités de maintenance à court terme sont :

- augmenter la couverture de tests automatisés ;
- renforcer les validations métier ;
- améliorer l’homogénéité de la gestion des erreurs ;
- continuer à documenter les évolutions du service.
