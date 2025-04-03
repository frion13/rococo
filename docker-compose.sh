#!/bin/zsh
set -e
set -x

echo '### Java version ###'
java --version
echo '### Gradle version ###'
gradle --version

front="./rococo-client/"
front_image="tatianalomanovskaya/rococo-client:latest"

export FRONT_IMAGE="$front_image"

echo "### Stopping and removing containers ###"
docker compose down || true

echo "### Removing old containers ###"
docker ps -a --filter "name=rococo" --format "{{.ID}}" | xargs -r docker rm -f || true

echo "### Removing old images ###"
docker images --format "{{.Repository}}:{{.Tag}}" | grep "rococo" | xargs -r docker rmi || true

echo "### Building backend ###"
./gradlew clean build jibDockerBuild -x :rococo-e2e-tests:test

echo "### Building frontend ###"
cd "$front" || exit
npm install && npm run build
docker build -t "$front_image" .
cd ..

echo "### Starting services ###"
docker compose up -d --build

echo "### Service status ###"
docker compose ps