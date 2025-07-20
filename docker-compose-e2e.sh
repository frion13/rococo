#!/bin/bash
source ./docker.properties
export PROFILE=local
export COMPOSE_PROFILES=test
export PREFIX="${IMAGE_PREFIX}"
export ALLURE_DOCKER_API=http://allure:5050/
export HEAD_COMMIT_MESSAGE="local build"
export ARCH=$(uname -m)

docker compose down

docker_containers=$(docker ps -a -q)
docker_images=$(docker images --format '{{.Repository}}:{{.Tag}}' | grep 'rococo')

if [ ! -z "$docker_containers" ]; then
  echo "### Stop containers: $docker_containers ###"
  docker stop $docker_containers
  docker rm $docker_containers
fi

if [ ! -z "$docker_images" ]; then
  echo "### Remove images: $docker_images ###"
  docker rmi $docker_images
fi

echo '### Java version ###'
java --version
bash ./gradlew clean

for image in "mysql:8.3" "confluentinc/cp-zookeeper:7.3.2" "confluentinc/cp-kafka:7.3.2" "${PREFIX}/rococo-auth-${PROFILE}:latest" "${PREFIX}/rococo-artist-${PROFILE}:latest" "${PREFIX}/rococo-museum-${PROFILE}:latest" "${PREFIX}/rococo-painting-${PROFILE}:latest" "${PREFIX}/rococo-userdata-${PROFILE}:latest" "${PREFIX}/rococo-gateway-${PROFILE}:latest" "aerokube/selenoid:1.11.3" "aerokube/selenoid-ui:1.10.11" "${PREFIX}/rococo-e2e-tests:latest" "frankescobar/allure-docker-service:2.27.0" "frankescobar/allure-docker-service-ui:7.0.3"; do

  if [[ "$(docker images -q "$image" 2> /dev/null)" == "" ]]; then
    bash ./gradlew clean
    bash ./gradlew jibDockerBuild -x :rococo-e2e-tests:test
    echo "### Собраны образы Rococo ###"
    break 2
  fi
done

docker-compose up -d rococo-all-db zookeeper kafka

sleep 15

for service in auth artist museum painting userdata gateway; do
  if ! docker inspect "${PREFIX}/rococo-${service}-${PROFILE}:latest" >/dev/null 2>&1; then
    echo "Ошибка: образ ${PREFIX}/rococo-${service}-${PROFILE}:latest не найден!"
    exit 1
  fi
done

docker pull selenoid/vnc_chrome:127.0

docker compose up -d
docker ps -a

docker-compose ps --filter "status=exited" --format "{{.Names}}" | while read -r container; do
  echo "=== Логи $container ==="
done