#!/usr/bin/env bash
set -e

echo "Démarrage de l'application en local avec Docker Compose..."

docker compose up -d

if [ ! -f .env ]; then
  echo "Fichier .env introuvable. Copiez .env.example vers .env et adaptez les valeurs."
  exit 1
fi

echo "Définition des variables d'environnement à partir du fichier .env..."

set -a
source .env
set +a

echo "Attente de PostgreSQL sur ${DB_HOST}:${DB_PORT}..."

until pg_isready -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" >/dev/null 2>&1; do
  sleep 2
done

echo "PostgreSQL est prêt. Démarrage du backend Spring Boot..."
./mvnw spring-boot:run