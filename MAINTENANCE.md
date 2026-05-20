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
- mettre à jour la documentation concernée si le comportement change ;
- vérifier que le contrat OpenAPI reste cohérent avec le code ;
- vérifier que les scénarios critiques restent fonctionnels : inscription, connexion, upload, historique, téléchargement et suppression.

### À chaque correction de bug

- reproduire le bug ;
- identifier la couche concernée ;
- corriger dans la bonne couche ;
- ajouter ou adapter un test automatisé si possible ;
- revalider le parcours concerné ;
- documenter la correction si elle modifie le comportement attendu.

### Maintenance mensuelle

- exécuter les tests backend ;
- générer l’arbre des dépendances Maven ;
- lancer le scan OWASP Dependency Check ;
- consulter les mises à jour disponibles des dépendances ;
- vérifier les vulnérabilités connues ;
- relire les points de dette technique ouverts ;
- vérifier la cohérence entre configuration, base et stockage local.

### Maintenance immédiate en cas d’alerte critique

Une maintenance immédiate doit être déclenchée en cas de vulnérabilité critique ou élevée concernant :

- Spring Boot ;
- Spring Security ;
- Tomcat embarqué ;
- PostgreSQL JDBC ;
- JJWT ;
- Jackson ;
- springdoc-openapi / Swagger UI.

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
- PostgreSQL JDBC ;
- Flyway ;
- JJWT ;
- Lombok ;
- springdoc-openapi ;
- Tomcat embarqué.

### Vérification des dépendances

La maintenance courante doit inclure au minimum :

```bash
./mvnw test
./mvnw dependency:tree
./mvnw versions:display-dependency-updates
./mvnw dependency-check:check
```

L’arbre des dépendances peut être exporté dans le dossier de suivi sécurité :

```bash
./mvnw dependency:tree | grep -v "Progress" > docs/security/maven-dependency-tree.txt
```

Le scan OWASP Dependency Check génère les rapports dans :

```text
docs/security/dependency-check/
```

### Vérification des vulnérabilités

Le backend dispose d’un scan OWASP Dependency Check versionné afin de suivre les vulnérabilités connues des dépendances Maven.

Les résultats doivent être analysés selon :

- la criticité de la vulnérabilité ;
- l’exposition réelle dans l’application ;
- le caractère direct ou transitif de la dépendance ;
- le risque de régression en cas de mise à jour immédiate.

Les vulnérabilités critiques ou élevées sur des composants exposés doivent être traitées en priorité.

### Règle de mise à jour

En cas de mise à jour de dépendance :

1. mettre à jour une dépendance ou un groupe cohérent de dépendances ;
2. reconstruire le projet ;
3. exécuter les tests ;
4. relancer le scan OWASP Dependency Check ;
5. vérifier les endpoints critiques :
    - login ;
    - register ;
    - upload ;
    - history ;
    - delete ;
    - bulk-delete ;
    - download ;
6. vérifier Swagger / OpenAPI ;
7. mettre à jour la documentation si le comportement, les versions ou les décisions de sécurité changent.

### Automatisation envisagée

Pour une mise en production, un outil comme Dependabot ou Renovate devra être activé afin de proposer automatiquement des pull requests de mise à jour des dépendances.

Les pull requests automatiques devront être validées par :

- compilation ;
- tests automatisés ;
- scan de sécurité ;
- vérification des endpoints critiques ;
- revue humaine avant merge.

## Matrice de risques techniques

| Zone / dépendance | Risque principal | Impact potentiel | Mesure de maintenance |
|---|---|---|---|
| Spring Security | Vulnérabilité sur l’authentification ou les filtres de sécurité | Accès non autorisé ou contournement de protection | Suivi des CVE, mise à jour prioritaire, tests sur routes protégées |
| JJWT | Mauvaise validation ou vulnérabilité sur les tokens | Authentification incorrecte ou erreurs 500 sur tokens invalides | Tests JWT valides, expirés et malformés |
| Tomcat embarqué | Vulnérabilité serveur HTTP | Risque d’exploitation réseau ou déni de service | Mise à jour via Spring Boot ou override contrôlé après tests |
| PostgreSQL JDBC | Vulnérabilité du driver de connexion | Risque sur la connexion BDD ou déni de service | Mise à jour Maven du driver, tests d’intégration |
| Flyway | Migration incorrecte ou désynchronisée | Schéma BDD incohérent avec les entités JPA | Migration versionnée, `ddl-auto=validate`, tests au démarrage |
| Upload fichiers | Fichier dangereux, trop volumineux ou path traversal | Saturation stockage, faille de sécurité ou fichier inaccessible | Validation taille/MIME, sanitisation, tests de sécurité |
| Stockage local | Fichiers orphelins ou incohérence disque/base | Accumulation de données ou téléchargement impossible | Purge planifiée, contrôle suppression disque + base |
| Swagger UI | Exposition de documentation technique | Surface d’information inutile en production | Désactivation ou protection hors environnement de développement |
| Configuration `.env` | Mauvaise configuration des secrets ou CORS | Échec démarrage, fuite de secret ou blocage front/back | `.env.example`, variables documentées, contrôle avant déploiement |
| Logs | Manque de corrélation ou d’observabilité | Diagnostic difficile en cas d’incident | Logs structurés et corrélation à prévoir en production |


## Base de données et persistance

Le backend manipule deux formes de persistance :

- PostgreSQL pour les utilisateurs et métadonnées ;
- le système de fichiers local pour les fichiers uploadés.

## Points de vigilance

- cohérence entre enregistrement en base et fichier réellement stocké ;
- cohérence lors de la suppression d’un fichier ;
- gestion des fichiers expirés ;
- prévention des fichiers orphelins ;
- vérification du dossier d’upload local ;
- validation des types MIME autorisés ;
- respect de la limite de taille maximale ;
- comportement des liens expirés ;
- distinction entre erreur `401 Unauthorized` et `403 Forbidden` ;
- cohérence entre configuration CORS locale et configuration de production.

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

- cohérence entre `.env`, `.env.example`, `application.yml` et profil `test` ;
- réglages multipart ;
- configuration PostgreSQL locale ;
- configuration CORS ;
- configuration JWT ;
- configuration du dossier `UPLOAD_DIR` ;
- configuration de sécurité Spring Security.

## Dette technique actuelle

Les points suivants restent identifiés comme dette technique ou sujets de maintenance à poursuivre :

- la couverture de branches backend reste perfectible ;
- la pagination de `GET /api/files` n’est pas encore mise en place ;
- le rate limiting de `POST /api/auth/login` reste à ajouter pour limiter le brute force ;
- le JWT est encore stocké côté frontend en `localStorage`, solution acceptable pour le MVP mais à durcir pour la production ;
- Swagger UI devra être désactivé ou protégé hors environnement de développement ;
- l’observabilité reste limitée : les logs applicatifs existent, mais il n’y a pas encore de corrélation avancée ni de métriques applicatives ;
- la gestion automatisée des dépendances peut être renforcée avec Dependabot ou Renovate ;
- la protection optionnelle par mot de passe des fichiers partagés, prévue en évolution, n’est pas livrée dans le MVP.

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
- les migrations Flyway éventuelles sont versionnées et cohérentes ;
- le contrat OpenAPI reste aligné avec le comportement de l’API ;
- la documentation impactée a été mise à jour si nécessaire ;
- la modification ne crée pas d’incohérence entre base et stockage local ;
- les changements de sécurité sont couverts par un test ou documentés explicitement ;
- les changements de dépendances sont accompagnés d’une vérification de sécurité.

## Conclusion

Le backend DataShare dispose d’une base maintenable pour un MVP, avec une séparation claire des couches, des tests automatisés, une documentation API, des migrations Flyway et un suivi de sécurité des dépendances.

Les priorités actuelles sont :

- poursuivre l’amélioration de la stratégie de dépendances ;
- automatiser la veille de sécurité avec Dependabot, Renovate ou une étape CI ;
- ajouter la pagination sur l’historique des fichiers ;
- ajouter un rate limiting sur l’endpoint de connexion ;
- préparer le durcissement production du stockage JWT ;
- améliorer l’observabilité ;
- conserver une cohérence stricte entre code, configuration, base de données, stockage local et documentation.
