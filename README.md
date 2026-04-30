# DataShare Backend

Backend Spring Boot de l’application DataShare.

## Description

Ce service expose l’API REST de l’application de partage sécurisé de fichiers.  
Il gère :
- l’authentification des utilisateurs 
- la gestion des fichiers 
- les métadonnées associées 
- la persistance PostgreSQL 
- le stockage local des fichiers uploadés

## Stack technique

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- Docker Compose
- Lombok

## Pré-requis

- JDK 21
- Maven
- Docker
- Docker Compose

## Configuration

La configuration locale repose sur un fichier `.env`.
Un fichier `.env.example` est fourni pour documenter les variables attendues.

Exemple :

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=datashare_db
DB_USER=datashare_user
DB_PASSWORD=datashare_password

JWT_SECRET=change-me-with-a-long-secret-key
JWT_EXPIRATION_MS=3600000
```

Créer ensuite un fichier `.env` local à partir de ce modèle.

## Démarrage en local

Le projet contient un script `run-local.sh` qui :

- démarre PostgreSQL avec Docker Compose 
- charge les variables d’environnement depuis `.env` 
- attend que PostgreSQL soit prêt 
- lance l’application Spring Boot

Commande :

```bash
./run-local.sh
```

L’application démarre ensuite sur :

```text
http://localhost:8080
```

Extrait de traces log attendu:
```bash
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v4.0.6)

2026-04-30T18:49:37.373+02:00  INFO 89584 --- [datashare-backend] [           main] d.DatashareBackendApplication            : Starting DatashareBackendApplication using Java 21.0.10 with PID 89584 (datashare-backend/target/classes started by jrj in datashare-backend)
2026-04-30T18:49:37.375+02:00  INFO 89584 --- [datashare-backend] [           main] d.DatashareBackendApplication            : No active profile set, falling back to 1 default profile: "default"
2026-04-30T18:49:37.753+02:00  INFO 89584 --- [datashare-backend] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-04-30T18:49:37.765+02:00  INFO 89584 --- [datashare-backend] [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 6 ms. Found 0 JPA repository interfaces.
2026-04-30T18:49:38.053+02:00  INFO 89584 --- [datashare-backend] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat initialized with port 8080 (http)
2026-04-30T18:49:38.067+02:00  INFO 89584 --- [datashare-backend] [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-04-30T18:49:38.067+02:00  INFO 89584 --- [datashare-backend] [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/11.0.21]
2026-04-30T18:49:38.103+02:00  INFO 89584 --- [datashare-backend] [           main] b.w.c.s.WebApplicationContextInitializer : Root WebApplicationContext: initialization completed in 702 ms
2026-04-30T18:49:38.259+02:00  INFO 89584 --- [datashare-backend] [           main] org.hibernate.orm.jpa                    : HHH008540: Processing PersistenceUnitInfo [name: default]
2026-04-30T18:49:38.288+02:00  INFO 89584 --- [datashare-backend] [           main] org.hibernate.orm.core                   : HHH000001: Hibernate ORM core version 7.2.12.Final
2026-04-30T18:49:38.512+02:00  INFO 89584 --- [datashare-backend] [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-04-30T18:49:38.534+02:00  INFO 89584 --- [datashare-backend] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-04-30T18:49:38.636+02:00  INFO 89584 --- [datashare-backend] [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@45832b85
2026-04-30T18:49:38.637+02:00  INFO 89584 --- [datashare-backend] [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-04-30T18:49:38.679+02:00  INFO 89584 --- [datashare-backend] [           main] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
...

2026-04-30T18:49:39.201+02:00  INFO 89584 --- [datashare-backend] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name inMemoryUserDetailsManager
2026-04-30T18:49:39.315+02:00  INFO 89584 --- [datashare-backend] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint beneath base path '/actuator'
2026-04-30T18:49:39.387+02:00  INFO 89584 --- [datashare-backend] [           main] o.s.boot.tomcat.TomcatWebServer          : Tomcat started on port 8080 (http) with context path '/'
2026-04-30T18:49:39.395+02:00  INFO 89584 --- [datashare-backend] [           main] d.DatashareBackendApplication            : Started DatashareBackendApplication in 2.251 seconds (process running for 2.434)
```

## Premier démarrage attendu

Lors du premier démarrage, le script :

- lance le conteneur PostgreSQL 
- attend que la base soit accessible 
- démarre ensuite l’application backend

Exemple de traces attendues :

```bash
Démarrage de l'application en local avec Docker Compose...
Définition des variables d'environnement à partir du fichier .env...
Attente de PostgreSQL sur localhost:5432...
PostgreSQL est prêt.
...
Tomcat started on port 8080 (http) with context path '/'
Started DatashareBackendApplication
```

## Base de données

Le backend utilise PostgreSQL.
Le conteneur est défini dans `compose.yaml`.
Les données sont persistées dans un volume Docker dédié.

## Structure du projet

Architecture cible :
```bash
src/main/java/.../
  configuration/security
  controller
  dto
  entities
  repository
  service
```
