<div align="center">

# 🧭 TourGuide

**Application de recommandation touristique**

[![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?style=flat-square&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)](https://github.com/features/actions)

[![CI](https://github.com/djerbiano/JavaPathENProject8/actions/workflows/ci.yml/badge.svg)](https://github.com/djerbiano/JavaPathENProject8/actions/workflows/ci.yml)
[![Tests](https://img.shields.io/badge/tests-12%2F12%20passing-brightgreen?style=flat-square)](#-tests--qualité)
[![Localisation](https://img.shields.io/badge/100k%20localisations-384%20s%20%2F%20900%20s-brightgreen?style=flat-square)](#-performance)
[![Récompenses](https://img.shields.io/badge/100k%20récompenses-518%20s%20%2F%201200%20s-brightgreen?style=flat-square)](#-performance)

</div>

<br/>

## 📋 Aperçu

TourGuide est une API REST construite avec **Spring Boot** pour le compte de l'entreprise fictive TourGuide, dont le nombre d'utilisateurs passe de 30 000 à 100 000. Elle localise les utilisateurs par GPS, leur propose les attractions touristiques les plus proches, calcule les points de récompense gagnés en visitant ces attractions et leur suggère des offres de voyage adaptées à leurs préférences.

L'application existait déjà, mais elle était trop lente et ses tests échouaient de façon intermittente. Le travail a consisté à **corriger les bugs de concurrence**, **compléter la fonctionnalité des attractions proches**, **paralléliser les appels aux services externes** pour tenir la charge de 100 000 utilisateurs, et **mettre en place un pipeline d'intégration continue**.

<br/>

## ✨ Points clés

| | |
|---|---|
| 🐛 **Tests intermittents corrigés** | `ConcurrentModificationException` entre le thread `Tracker` et les requêtes : listes passées en `CopyOnWriteArrayList`, `addUserReward` rendu atomique avec `synchronized`, bug de comparaison `String` / `Attraction` corrigé |
| 📍 **5 attractions les plus proches** | Tri par distance + `limit(5)` au lieu d'un filtre à 200 miles, réponse JSON dédiée (`NearbyAttractionDTO`, 7 champs) |
| ⚡ **Parallélisme** | `CompletableFuture` sur des pools de 100 threads (`ExecutorService`) pour gpsUtil et RewardCentral, fermés proprement (shutdown hook, `@PreDestroy`) |
| 🚀 **Performance** | Localisation de 100 000 utilisateurs ≈ **20× plus rapide**, calcul des récompenses ≈ **124× plus rapide** |
| 🔄 **Intégration continue** | Pipeline GitHub Actions : compilation, tests (y compris performance), construction et publication du jar |
| 🧹 **Qualité** | Injection par constructeur, `@GetMapping`, bean en double supprimé, tests existants jamais modifiés |

<br/>

## 🌐 Endpoints

Tous les endpoints sont en `GET` et prennent un paramètre `userName` (utilisateurs de test : `internalUser0` à `internalUser99`).

| Endpoint | Description |
|---|---|
| `/` | Message d'accueil |
| `/getLocation` | Dernière position connue de l'utilisateur |
| `/getNearbyAttractions` | Les 5 attractions les plus proches, quelle que soit la distance |
| `/getRewards` | Récompenses gagnées par l'utilisateur |
| `/getTripDeals` | Offres de voyage selon les préférences de l'utilisateur |

**Exemple** — `GET /getNearbyAttractions?userName=internalUser0` (un objet sur les 5, valeurs illustratives : les positions des utilisateurs de test sont aléatoires)
```json
[
  {
    "attractionName": "Disneyland",
    "attractionLatitude": 33.817595,
    "attractionLongitude": -117.922008,
    "userLatitude": 33.8,
    "userLongitude": -118.1,
    "distance": 10.27,
    "rewardPoints": 482
  }
]
```

<br/>

## 🏗️ Architecture

```
TourGuide/src/main/java/com/openclassrooms/tourguide/
├── TourGuideController.java   → endpoints REST
├── TourGuideModule.java       → beans des librairies externes (GpsUtil, RewardCentral)
├── service/                   → TourGuideService, RewardsService (logique métier + pools de threads)
├── tracker/                   → Tracker, thread qui relocalise tous les utilisateurs toutes les 5 min
├── dto/                       → NearbyAttractionDTO
├── user/                      → User, UserPreferences, UserReward
└── helper/                    → InternalTestHelper (nombre d'utilisateurs de test)
```

**Flux type**
```
Requête HTTP → Controller → TourGuideService → gpsUtil       (position GPS)
                                    ↓
                             RewardsService  → RewardCentral (points de récompense)
                                    ↓
                             TripPricer                      (offres de voyage)
```

<br/>

- `gpsUtil`, `RewardCentral` et `TripPricer` sont des librairies fournies (`TourGuide/libs/`) qui simulent des services distants lents.
- Le temps est passé à **attendre**, pas à calculer : on fait donc attendre beaucoup d'utilisateurs en même temps, au lieu de les traiter un par un.
- `trackAllUsersLocation` et `calculateAllUsersRewards` lancent une tâche par utilisateur sur le pool, puis attendent la fin de toutes avec `CompletableFuture.allOf(...).join()`.
- Chaque service a son propre pool : `TourGuideService` utilise `RewardsService`, pas l'inverse.

<br/>

## ⚡ Performance

Mesures faites avec `TestPerformance` (temps « avant » fournis par OpenClassrooms, temps « après » mesurés en local).

| Utilisateurs | Localisation avant | Localisation après | Récompenses avant | Récompenses après |
|---:|---:|---:|---:|---:|
| 100 | 7 s | **0 s** | 44 s | **1 s** |
| 1 000 | 75 s | **3 s** | 472 s | **5 s** |
| 10 000 | 762 s | **39 s** | 5 820 s | **52 s** |
| 100 000 | 7 579 s (2 h 06) | **384 s** | 64 020 s (17 h 47) | **518 s** |
| **Limite** | | **900 s** | | **1 200 s** |

> Le temps de localisation à 100 000 utilisateurs (384 s) inclut le travail du `Tracker`, qui démarre avec l'application et localise lui aussi tous les utilisateurs en parallèle, dans le même pool. Sans lui, le test seul descend à 201 s.

<br/>

## 🧪 Tests & qualité

| Métrique | Résultat |
|---|---|
| Tests | **12 / 12** (0 échec, 0 ignoré) |
| Tests de performance | 100 000 utilisateurs, activés aussi dans la CI |
| Tests existants modifiés | **Aucun** (seuls les `@Disabled` ont été retirés) |

```
TestTourGuideService       → utilisateurs, localisation, suivi, attractions proches, DTO (6 tests)
TestRewardsService         → calcul des récompenses, proximité des attractions (3 tests)
TestPerformance            → highVolumeTrackLocation, highVolumeGetRewards (100 000 utilisateurs)
TourguideApplicationTests  → démarrage du contexte Spring
```

```bash
mvn clean test
```

> La suite complète dure environ 15 minutes à cause des tests de performance. Pour les exclure : `mvn test -Dtest='!TestPerformance'`.

<br/>

## 🔄 Intégration continue

Le pipeline [`.github/workflows/ci.yml`](.github/workflows/ci.yml) se déclenche à chaque `push` et `pull request` sur `master` :

```
Checkout → Java 17 (cache Maven) → install des 3 jars locaux → compile → test → package → upload du jar
```

Le jar construit est téléchargeable en artefact (`tourguide-jar`) depuis la page de chaque exécution.

<br/>

## 📖 Documentation

Documentation technique complète (contexte, architecture, diagramme de séquence, choix techniques et solutions écartées) :

**👉 [TourGuide_documentation.pdf](docs/TourGuide_documentation.pdf)**

<br/>

## 🚀 Installation

```bash
git clone https://github.com/djerbiano/JavaPathENProject8.git
cd JavaPathENProject8/TourGuide
```

Installe les trois librairies locales dans ton dépôt Maven :

```bash
mvn install:install-file -Dfile=libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar
```

Puis lance l'application :

```bash
mvn spring-boot:run
```

L'API est disponible sur `http://localhost:8080`, par exemple `http://localhost:8080/getNearbyAttractions?userName=internalUser0`.

<br/>

## 🛠️ Stack technique

**Backend** — Java 17 · Spring Boot 3.1.1 · Spring Web · Maven · `CompletableFuture` / `ExecutorService`

**Librairies fournies** — gpsUtil · RewardCentral · TripPricer

**Tests & CI** — JUnit 5 · Spring Boot Test · GitHub Actions

<br/>
