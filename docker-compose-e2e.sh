#!/bin/bash
set -e
set -x

source ./docker.properties
export PROFILE=docker
export PREFIX="${IMAGE_PREFIX}"
export ARCH=$(uname -m)
export ALLURE_DOCKER_API=http://allure:5050/
export BUILD_URL="-http://localhost:5050"
export EXECUTION_TYPE="-docker"
export HEAD_COMMIT_MESSAGE="-Local Docker run"
export FRONT_VERSION="0.0.1"
export COMPOSE_PROFILES=test

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
bash ./gradlew jibDockerBuild -x :rococo-e2e-tests:test

docker pull selenoid/vnc_chrome:127.0
docker compose up -d
docker ps -a