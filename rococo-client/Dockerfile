FROM node:22.6.0-alpine AS build

ARG NPM_COMMAND=build
ARG VERSION=latest

WORKDIR /app
COPY package.json ./
COPY package-lock.json ./
RUN npm install

COPY . ./

RUN npm run ${NPM_COMMAND}

FROM nginx:1.27.1-alpine AS release

ARG VERSION
ENV VERSION=${VERSION}
ENV TZ=Europe/Moscow

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /app/build /usr/share/nginx/html/

CMD ["nginx", "-g", "daemon off;"]