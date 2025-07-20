#!/bin/zsh
set -e
set -x

# Load environment variables from docker.properties file
source ./docker.properties

# Set deployment configuration
export PROFILE=docker
export PREFIX="${IMAGE_PREFIX}"
export ARCH=$(uname -m)
export FRONT_VERSION="1.0.0"

# Pull base infrastructure images
docker-compose pull rococo-all-db zookeeper kafka

echo "### Stopping and removing containers ###"
docker compose down || true

# Build mode selection
if [ "$1" = "push" ] || [ "$2" = "push" ]; then
  echo "### Building and pushing images ###"
  bash ./gradlew jib -x :rococo-e2e-tests:build
  docker compose push frontend.rococo.dc
else
  echo "### Local image building ###"
  bash ./gradlew jibDockerBuild -x :rococo-e2e-tests:build
fi

echo "### Starting DB, Kafka ###"
docker-compose up -d rococo-all-db zookeeper kafka
sleep 10

echo "### Verify Docker images exist ###"
for service in auth artist museum painting userdata gateway; do
  if ! docker inspect "${PREFIX}/rococo-${service}-${PROFILE}:latest" >/dev/null 2>&1; then
    echo "Error: Image ${PREFIX}/rococo-${service}-${PROFILE}:latest not found!"
    exit 1
  fi
done

echo "### Start ###"
docker-compose up -d || {
  echo "### error ###"
  docker-compose logs
  exit 1
}

echo "### status ###"
docker-compose ps

echo "### logs###"
docker-compose ps --filter "status=exited" --format "{{.Names}}" | while read -r container; do
  echo "=== Логи $container ==="
  docker logs "$container" || echo "Can'n get logs for $container"
done
