# MAINTENANCE.md

## Objectif

Ce document décrit les règles de maintenance du backend Spring Boot de DataShare.

L’objectif est de garantir dans le temps :

- la stabilité du service ;
- la sécurité des dépendances ;
- la capacité à corriger rapidement un bug ;
- la cohérence entre base de données, stockage local et API ;
- la possibilité de faire évoluer le backend sans dégrader les fonctionnalités existantes.

## Périmètre

Le backend couvre principalement :

- l’authentification ;
- la gestion des fichiers ;
- l’accès aux téléchargements publics ;
- la persistance PostgreSQL ;
- le stockage local des fichiers ;
- la configuration de sécurité.

## Organisation du projet

Le backend est structuré de manière à séparer clairement :

- la configuration ;
- la sécurité ;
- les contrôleurs REST ;
- les DTO ;
- les entités ;
- les repositories ;
- les services métier ;
- la gestion des exceptions.

Structure principale :

```text
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

Cette organisation facilite l’identification des responsabilités et limite le couplage entre couches.

## Principes de maintenance retenus

Les principes suivants guident la maintenance du backend :

- conserver une séparation stricte entre couche web, logique métier et accès aux données ;
- centraliser les règles métier dans les services ;
- centraliser les règles de sécurité dans la configuration dédiée ;
- éviter d’introduire de la logique métier dans les contrôleurs ;
- maintenir une documentation alignée avec l’état réel du projet ;
- traiter en priorité les régressions sur l’authentification, l’upload et la suppression.

## Fréquence de maintenance recommandée

### À chaque évolution fonctionnelle

- exécuter les tests backend ;
- vérifier les endpoints impactés ;
- mettre à jour la documentation concernée si le comportement change.

### À chaque correction de bug

- reproduire le bug ;
- corriger dans la bonne couche ;
- ajouter ou adapter un test si possible ;
- revalider le parcours concerné.

### Au minimum une fois par sprint ou une fois par mois
- revoir les dépendances Maven ;
- vérifier les vulnérabilités connues ;
- relire les points de dette technique ouverts ;
- vérifier la cohérence entre configuration, base et stockage local.

## Procédure de maintenance corrective

En cas de bug backend, la démarche recommandée est :

1. reproduire le problème ;
2. identifier la couche concernée :
    - sécurité ;
    - contrôleur ;
    - service ;
    - repository ;
    - stockage local ;
    - configuration ;
3. consulter les logs applicatifs et le comportement HTTP observé ;
4. corriger dans la couche appropriée ;
5. valider par test automatisé et/ou manuel ;
6. mettre à jour la documentation si le comportement attendu change.

## Procédure de maintenance évolutive

Lorsqu’une évolution est ajoutée :

1. vérifier si elle impacte :
    - les DTO ;
    - les services ;
    - les règles de sécurité ;
    - la structure de données ;
    - le stockage local ;
2. vérifier si l’API OpenAPI doit être mise à jour ;
3. vérifier si les fichiers `README.md`, `TESTING.md`, `SECURITY.md`, `PERF.md` ou `TECHNICAL-DOCUMENTATION.md` doivent être modifiés ;
4. exécuter les tests et revalider les parcours principaux.

## Dépendances et mises à jour

Le backend repose principalement sur :

- Spring Boot ;
- Spring Security ;
- Spring Data JPA ;
- PostgreSQL ;
- Lombok ;
- springdoc-openapi.

### Vérification des dépendances

La maintenance courante doit inclure au minimum :
```bash
./mvnw test
./mvnw dependency:tree
./mvnw versions:display-dependency-updates
```

### Vérification des vulnérabilités

Le projet ne dispose pas encore d’une chaîne d’audit de sécurité Maven totalement industrialisée.
La revue des dépendances doit donc être faite de manière régulière à partir :

- des dépendances Maven du projet ;
- des bulletins de sécurité des bibliothèques principales ;
- des mises à jour Spring Boot et Spring Security.

### Règle de mise à jour

En cas de mise à jour de dépendance :

1. mettre à jour une dépendance ou un groupe cohérent de dépendances ;
2. reconstruire le projet ;
3. exécuter les tests ;
4. vérifier les endpoints critiques :
    - login ;
    - register ;
    - upload ;
    - history ;
    - delete ;
    - download ;
5. mettre à jour la documentation si le comportement ou les versions changent.

## Base de données et persistance

Le backend manipule deux formes de persistance :

- PostgreSQL pour les utilisateurs et métadonnées ;
- le système de fichiers local pour les fichiers uploadés.

## Points de vigilance

- cohérence entre enregistrement en base et fichier réellement stocké ;
- cohérence lors de la suppression d’un fichier ;
- gestion des fichiers expirés ;
- prévention des fichiers orphelins ;
- vérification du dossier d’upload local.

## Règle de maintenance

Lorsqu’un problème touche l’upload ou la suppression, il faut systématiquement vérifier :

- la ligne en base ;
- la présence ou l’absence du fichier sur disque ;
- les logs applicatifs ;
- la configuration UPLOAD_DIR.

## Zones sensibles du backend

Les zones suivantes doivent être surveillées en priorité :

### Authentification et sécurité

- génération et validation des JWT ;
- filtrage des routes protégées ;
- séparation entre routes publiques et routes authentifiées.

### Gestion des fichiers

- validation de type et de taille ;
- stockage disque ;
- suppression ;
- cohérence entre stockage physique et métadonnées.

### Téléchargement public

- gestion des tokens ;
- expiration des liens ;
- disponibilité réelle du fichier stocké.

### Configuration
- cohérence entre `.env`, `application.yml` et profil `test` ;
- réglages multipart ;
- configuration PostgreSQL locale ;
- configuration de sécurité.

## Dette technique actuelle

Les points suivants restent identifiés comme dette technique ou sujets de maintenance à poursuivre :

- la couverture de branches reste perfectible ;
- la stratégie de migration de base de données n’est pas encore industrialisée ;
- le cycle de vie complet des fichiers expirés doit être renforcé ;
- l’observabilité reste limitée ;
- la gestion des dépendances peut être davantage outillée.

## Bonnes pratiques recommandées

Pour maintenir le backend dans de bonnes conditions :

- garder les contrôleurs fins ;
- centraliser la logique métier dans les services ;
- centraliser les exceptions et réponses d’erreur ;
- éviter les changements implicites de configuration ;
- tester les parcours critiques à chaque évolution ;
- documenter les changements importants dans les fichiers de suivi du projet.

## Critères minimaux avant merge d’un changement backend

Avant de fusionner une évolution backend, vérifier au minimum :

- le projet compile ;
- `./mvnw test` passe ;
- les endpoints impactés ont été testés ;
- la documentation impactée a été mise à jour si nécessaire ;
- la modification ne crée pas d’incohérence entre base et stockage local.

## Conclusion

Le backend DataShare dispose d’une base maintenable pour un MVP, mais sa maintenance doit rester active et structurée.

Les priorités actuelles sont :

- poursuivre l’amélioration de la stratégie de dépendances ;
- renforcer le cycle de vie des données et des fichiers ;
- améliorer l’observabilité ;
- conserver une cohérence stricte entre code, configuration et documentation.
