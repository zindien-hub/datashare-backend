# PERF.md

## Objectif

Ce document décrit les premières mesures de performance réalisées sur le backend DataShare.

L’objectif est de vérifier le comportement d’un endpoint critique du MVP sous une charge légère et contrôlée.

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

## Scénario de test

Le scénario exécuté est le suivant :

- 5 utilisateurs virtuels ;
- durée de 30 secondes ;
- upload répété d’un petit fichier texte ;
- envoi d’un JWT valide dans l’en-tête `Authorization`.

### Seuils définis

- `http_req_failed < 5 %`
- `p(95) < 2000 ms`

## Résultats obtenus

### Résultats globaux

- 150 requêtes HTTP ;
- 150 itérations complètes ;
- 0 erreur ;
- 100 % des checks validés.

### Temps de réponse

- moyenne : **23.37 ms**
- médiane : **18.78 ms**
- p90 : **23.42 ms**
- p95 : **35.44 ms**
- maximum : **141.24 ms**

### Fiabilité

- taux d’échec HTTP : **0.00 %**

### Exécution

- 5 VUs constants ;
- durée effective : environ 30 secondes ;
- débit observé : **4.88 requêtes/seconde**

## Interprétation

Les résultats montrent que, dans un environnement local et sous charge légère :

- l’endpoint `POST /api/files` répond rapidement ;
- le système reste stable sur toute la durée du test ;
- aucun échec n’a été constaté ;
- les seuils de performance définis pour ce test sont respectés.

Le backend supporte donc correctement un premier niveau de charge sur une fonctionnalité critique du MVP.

## Limites actuelles

Ces mesures ont été obtenues dans un contexte limité :

- exécution locale ;
- fichier de petite taille ;
- faible nombre d’utilisateurs virtuels ;
- absence de campagne comparative sur plusieurs tailles de fichiers ;
- absence de test longue durée ;
- absence de mesure sur un environnement de production ou préproduction.

Ces résultats doivent donc être interprétés comme une validation initiale du prototype, et non comme une garantie de performance à grande échelle.

## Optimisations et approfondissements possibles

Les améliorations envisageables sont :

- tester plusieurs tailles de fichiers ;
- mesurer l’impact du stockage local sur des volumes plus importants ;
- tester des montées en charge progressives ;
- ajouter des mesures sur les endpoints `GET /api/files` et `GET /download/{token}` ;
- compléter avec des métriques système plus fines (CPU, mémoire, I/O disque).
