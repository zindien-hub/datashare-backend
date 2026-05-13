# PERF.md

## Objectif

Ce document décrit les mesures de performance réalisées sur le backend DataShare.

L’objectif est d’évaluer le comportement de l’endpoint principal d’upload dans plusieurs scénarios de charge, sur différentes tailles de fichiers, afin d’obtenir des résultats plus représentatifs que le premier test initial.

## Endpoint testé

L’endpoint principal retenu pour ce test est :

- `POST /api/files`

Cet endpoint a été choisi car il représente un flux métier central de l’application :

- authentification via JWT ;
- réception d’un fichier ;
- écriture sur disque ;
- persistance des métadonnées en base.

## Outil utilisé

Le test a été réalisé avec **k6**.

## Scénarios de test

Deux niveaux de scénarios ont été exécutés.

### 1. Scénario de charge progressive modérée

Un premier scénario a été mis en place avec une montée en charge progressive jusqu’à **15 VUs**, sur plusieurs tailles de fichiers :

- `1 KB`
- `1 MB`
- `4 MB`

Le script utilisait :
- une montée progressive ;
- un `sleep(1)` entre les itérations ;
- des seuils de validation sur le taux d’erreur, les checks et le `p95`.

Ce scénario a permis de valider un comportement stable dans le périmètre nominal de l’application.

### 2. Scénario de stress court

Un second scénario plus exigeant a ensuite été mis en place pour répondre plus directement à la remarque sur le caractère trop trivial du test initial.

Ce scénario reposait sur :

- une rampe progressive jusqu’à **40 VUs** ;
- l’absence de `sleep(1)` entre les itérations ;
- un test sur trois tailles de fichiers :
  - `1 KB`
  - `1 MB`
  - `4 MB`

Les tailles testées ont été choisies pour couvrir plusieurs ordres de grandeur tout en restant cohérentes avec le périmètre nominal actuel de l’application, dont la limite d’upload a été volontairement fixée à **5 MB**.

## Seuils définis

### Scénario modéré
- `http_req_failed < 5 %`
- `p(95) < 4000 ms`
- `checks > 95 %`

### Scénario de stress court
- `http_req_failed < 10 %`
- `p(95) < 4000 ms`
- `checks > 90 %`

## Résultats obtenus

## A. Résultats du scénario de charge progressive modérée (jusqu’à 15 VUs)

### Fichier 1 KB
- moyenne : **13.53 ms**
- médiane : **12.86 ms**
- p90 : **17.41 ms**
- p95 : **19.23 ms**
- maximum : **37.76 ms**
- taux d’échec HTTP : **0.00 %**
- checks réussis : **100 %**
- débit observé : **8.80 req/s**

### Fichier 1 MB
- moyenne : **65.96 ms**
- médiane : **15.39 ms**
- p90 : **18.16 ms**
- p95 : **19.22 ms**
- maximum : **7.42 s**
- taux d’échec HTTP : **0.00 %**
- checks réussis : **100 %**
- débit observé : **8.54 req/s**

### Fichier 4 MB
- moyenne : **26.28 ms**
- médiane : **25.69 ms**
- p90 : **30.02 ms**
- p95 : **32.28 ms**
- maximum : **80.31 ms**
- taux d’échec HTTP : **0.00 %**
- checks réussis : **100 %**
- débit observé : **8.63 req/s**

## B. Résultats du scénario de stress court (jusqu’à 40 VUs, sans `sleep(1)`)

### Fichier 1 KB
- moyenne : **44.51 ms**
- médiane : **23.63 ms**
- p90 : **47.05 ms**
- p95 : **66.06 ms**
- maximum : **2.53 s**
- taux d’échec HTTP : **0.00 %**
- checks réussis : **100 %**
- débit observé : **516 req/s**

### Fichier 1 MB
- moyenne : **112.86 ms**
- médiane : **40.55 ms**
- p90 : **134.43 ms**
- p95 : **684.37 ms**
- maximum : **7.09 s**
- taux d’échec HTTP : **0.00 %**
- checks réussis : **100 %**
- débit observé : **203 req/s**

### Fichier 4 MB
- moyenne : **204.92 ms**
- médiane : **94.88 ms**
- p90 : **458.56 ms**
- p95 : **895.41 ms**
- maximum : **19.25 s**
- taux d’échec HTTP : **0.00 %**
- checks réussis : **100 %**
- débit observé : **102 req/s**

## Interprétation

Les résultats montrent que :

- l’endpoint `POST /api/files` reste stable en environnement local sur plusieurs tailles de fichiers ;
- aucune erreur n’a été observée sur les scénarios retenus ;
- l’augmentation de la taille des fichiers entraîne une hausse cohérente de la latence et une baisse du débit ;
- même dans le scénario de stress court jusqu’à 40 VUs, les seuils définis ont été respectés.

Le premier scénario confirme la stabilité sous charge modérée.

Le second scénario apporte une mesure plus significative de robustesse locale, en supprimant le plafonnement artificiel introduit par `sleep(1)` et en augmentant nettement la concurrence.

Quelques pics isolés de latence ont toutefois été observés, notamment :
- **7.42 s** sur le scénario 1 MB modéré ;
- **7.09 s** sur le scénario 1 MB en stress court ;
- **19.25 s** sur le scénario 4 MB en stress court.

Ces pics restent ponctuels et n’ont pas entraîné d’erreur, mais ils montrent que l’environnement local n’offre pas une stabilité parfaitement linéaire sous charge.

## Ajustements techniques réalisés pendant les tests

Les campagnes de test ont mis en évidence un point de configuration important :

- l’application validait déjà une limite métier d’upload côté service ;
- mais la configuration multipart de Spring n’était pas alignée avec cette limite.

La configuration suivante a donc été ajoutée :

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB
```

Cette correction a permis de rendre cohérente la gestion des uploads volumineux avec la règle d’acceptation définie dans l’application.

## Limites actuelles

Ces mesures ont été obtenues dans un cadre encore limité :

- exécution locale uniquement ;
- pas d’environnement de préproduction ou de production ;
- pas de métriques système détaillées (CPU, mémoire, I/O disque) ;
- pas de comparaison sur plusieurs machines ou plusieurs configurations ;
- pas de campagne longue durée ;
- pas de test à + 5 MB, car cela sortirait du périmètre nominal actuellement retenu pour l’application.

Les résultats doivent donc être interprétés comme une validation sérieuse de la robustesse locale du MVP, et non comme une garantie de performance à grande échelle.

## Optimisations et approfondissements possibles

Les améliorations envisageables sont :

- tester également les endpoints `GET /api/files` et `GET /download/{token}` ;
- ajouter des métriques système pendant les campagnes k6 ;
- comparer plusieurs profils de charge et plusieurs environnements ;
- mesurer l’impact du stockage local sur des volumes plus importants ;
- compléter avec une stratégie d’observabilité plus riche (logs structurés, corrélation, métriques techniques) ;
- réévaluer la limite d’upload si le besoin métier évolue.