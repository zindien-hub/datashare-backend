# AI-USAGE.md

## Objectif

Ce document décrit l’usage de l’intelligence artificielle pendant le développement de DataShare, conformément aux attendus du projet.

L’objectif est de préciser :
- quelles tâches ont été confiées à l’IA ;
- sur quelles user stories l’IA a été utilisée ;
- quel a été le rôle de supervision humaine ;
- quels ajustements ont été nécessaires avant intégration ;
- comment la traçabilité de cet usage est visible dans l’historique Git.

## Périmètre de l’usage de l’IA

L’IA n’a pas été utilisée sur l’ensemble du projet, mais comme assistant ciblé sur des évolutions limitées et identifiées.

Deux usages principaux ont été retenus :

- un premier usage côté frontend, centré sur une amélioration d’ergonomie de la page d’historique ;
- un second usage plus structurant, impliquant le frontend et le backend, autour de la suppression multiple de fichiers.

L’IA a été utilisée comme outil d’assistance à la proposition d’implémentation, à la structuration du code et à la revue technique, avec validation humaine systématique avant intégration.

## Usage IA n°1 : amélioration UX de la page History

### User story retenue

Premier usage retenu : **amélioration de l’expérience utilisateur sur la page d’historique des fichiers**.

Cette user story couvrait :
- la navigation depuis l’historique vers la page d’upload ;
- la copie du lien de téléchargement depuis l’historique ;
- une amélioration de l’ergonomie des actions disponibles sur la page.

### Tâches confiées à l’IA

L’IA a été sollicitée pour :
- proposer une amélioration UX de la page `/history` ;
- suggérer l’ajout d’un bouton de retour vers `/upload` ;
- proposer l’ajout d’une action de copie du lien de téléchargement ;
- produire une première base d’implémentation frontend.

### Limites de cet usage

Cet usage est resté relativement limité sur le plan fonctionnel.

Il a permis une amélioration visible de l’interface, mais portait surtout sur :
- l’ergonomie ;
- le confort d’utilisation ;
- des interactions utilisateur simples.

Il ne constituait donc pas à lui seul une évolution métier significative.

## Usage IA n°2 : suppression multiple de fichiers

### User story retenue

Second usage retenu : **ajout de la sélection multiple et de la suppression groupée de fichiers depuis l’historique**.

Cette user story couvre :
- la sélection de plusieurs fichiers dans la page `/history` ;
- la suppression groupée côté frontend ;
- la création d’un endpoint backend dédié ;
- l’ajout des appels de service correspondants ;
- l’ajout et l’adaptation des tests frontend et backend.

### Tâches confiées à l’IA

L’IA a été utilisée pour :
- proposer une solution fonctionnelle pour la sélection multiple dans l’historique ;
- suggérer la structure de l’évolution côté composant Angular ;
- proposer l’évolution du service frontend de fichiers ;
- proposer une implémentation backend pour la suppression multiple ;
- générer une première version des tests associés ;
- servir de base à une revue de code détaillée avant intégration.

### Résultat initial fourni par l’IA

Le code proposé par l’IA couvrait bien le besoin fonctionnel dans son principe :
- ajout d’une sélection multiple dans l’interface ;
- ajout d’une action de suppression groupée ;
- ajout d’un endpoint backend de suppression multiple ;
- ajout d’un DTO de requête ;
- ajout de tests backend et frontend.

Cependant, cette proposition initiale n’a pas été intégrée telle quelle.

## Rôle de supervision humaine

Mon rôle a consisté à encadrer, relire, corriger et valider les propositions de l’IA à chaque étape.

Concrètement, j’ai :
- défini le périmètre fonctionnel à traiter ;
- sélectionné les propositions pertinentes ;
- écarté les choix jugés trop fragiles, inutiles ou incohérents ;
- adapté le code à l’architecture réelle du projet ;
- harmonisé le style de code avec l’existant ;
- complété ou corrigé les tests ;
- exécuté les validations backend et frontend ;
- décidé des versions finales réellement conservées dans le projet.

L’IA a donc été utilisée comme assistant de développement, et non comme mécanisme d’intégration automatique.

## Ajustements et correctifs réalisés après proposition IA

Les propositions initiales ont nécessité une phase réelle de relecture et de correction avant intégration.

### Ajustements réalisés sur le premier usage

Pour l’amélioration UX de la page History, les ajustements retenus ont notamment consisté à :
- supprimer les commentaires explicites liés à l’IA dans le code source ;
- harmoniser visuellement les boutons avec le style existant ;
- ajouter un contrôle plus robuste autour de l’usage du presse-papiers ;
- compléter l’accessibilité et l’ergonomie avec des attributs `title`.

### Ajustements réalisés sur le second usage

Pour la suppression multiple, les ajustements humains ont notamment consisté à :
- revoir la cohérence globale entre frontend et backend ;
- compléter les tests unitaires frontend sur le service et le composant History ;
- ajouter un état dédié côté frontend pour éviter les doubles actions pendant la suppression multiple ;
- désactiver les actions concurrentes pendant une suppression groupée ;
- améliorer la robustesse du service backend sur les sélections invalides ;
- compléter les tests backend sur les cas d’erreur ;
- ajouter une garde défensive côté contrôleur backend lorsque la requête est absente ;
- harmoniser le formatage et les conventions de code avec le reste du projet.

Ces corrections montrent que la proposition IA a servi de base de travail, mais que le résultat final repose sur une intégration et une validation humaines.

## Vérifications réalisées

Après corrections, les vérifications suivantes ont été effectuées :

### Frontend
- exécution des tests unitaires frontend ;
- vérification de la couverture frontend ;
- validation du comportement de la page History après ajout de la sélection multiple ;
- contrôle du bon enchaînement entre sélection, suppression et rechargement de l’historique.

### Backend
- exécution des tests backend ;
- vérification de la couverture backend ;
- contrôle du comportement du contrôleur et du service de suppression multiple ;
- validation des cas d’erreur gérés côté service et contrôleur.

## Traçabilité dans Git

La traçabilité de l’usage de l’IA est visible dans l’historique Git à travers :
- des branches dédiées ;
- des commits distincts ;
- des pull requests séparées côté frontend et côté backend ;
- une phase identifiable de revue et de correction humaine après proposition initiale.

Cette traçabilité permet de distinguer :
- la base de travail proposée avec assistance IA ;
- les ajustements réalisés manuellement ;
- les validations menées avant merge.

## Bilan

L’IA a apporté une aide utile pour :
- accélérer la production d’une première base d’implémentation ;
- proposer une structure de code sur des évolutions ciblées ;
- faciliter l’exploration de solutions possibles ;
- servir de support à une revue de code détaillée.

En revanche, le résultat final repose sur :
- une supervision humaine continue ;
- des arbitrages techniques ;
- des corrections de robustesse ;
- l’harmonisation avec l’architecture existante ;
- l’exécution des tests ;
- la validation finale avant intégration.

L’usage de l’IA dans ce projet a donc été celui d’un assistant de développement sous contrôle humain, avec une contribution plus significative sur la suppression multiple que sur la première amélioration purement ergonomique.