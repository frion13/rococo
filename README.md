# Rococo - платформа для изучения искусства
<img src="images/logo.png" width="200" alt="Rococo Logo">

**Микросервисная платформа для работы с произведениями искусства**

## Содержание
- [Технологии](#технологии)
- [Архитектура](#архитектура)
- [Запуск приложения локально в IDE](#запуск_приложения_в_ide)
- [Запуск тестов локально](#запуск_тестов_локально)
- [Запуск в Docker](#запуск_в_docker)


<a name="технологии"></a>
## 🛠 Технологический стек

### Основные технологии
- **Java 21**
- **Spring Boot 3** с модулями:
  - Spring Security (OAuth2)
  - Spring Data JPA
  - Spring Kafka
  - Spring gRPC
- **Базы данных**:
  - MySQL 8.3 (основная БД)
  - Flyway (миграции)
- **Межсервисное взаимодействие**:
  - gRPC для
  - REST API
  - Kafka для событийной модели

### Инфраструктура
- Docker + Docker Compose
- Selenoid для UI-тестов
- Allure для отчетов

<a name="архитектура"></a>
## 🏛 Архитектура системы


# Rococo - Архитектура микросервисов

## Компоненты системы

### 🛡 Auth Service
**Назначение**: Централизованная аутентификация и авторизация  
**Технологии**: Spring Security, OAuth2  
**Порт**: 9000 (HTTP)  
**Особенности**:
- Генерация JWT токенов
- Управление ролями пользователей

### 🚪 Gateway Service
**Назначение**: Единая точка входа для клиентов  
**Технологии**: Spring Cloud Gateway  
**Порт**: 8090 (HTTP)  
**Функции**:
- Маршрутизация запросов
- Балансировка нагрузки
- Трансформация REST↔gRPC

### 👤 Userdata Service
**Назначение**: Управление профилями пользователей  
**Технологии**: gRPC, JPA  
**Порт**: 8091 (gRPC)  
**Данные**:
- Персональная информация
- Аватары
- Настройки профиля

### 🌍 Geo Service
**Назначение**: Географические справочники  
**Технологии**: gRPC, Cache  
**Порт**: 8094 (gRPC)  
**Данные**:
- Страны
- Города

### 🏛 Museum Service
**Назначение**: Каталог музеев  
**Технологии**: gRPC, Elasticsearch  
**Порт**: 8093 (gRPC)  
**Данные**:
- Описания музеев
- Коллекции


### 🎨 Artist Service
**Назначение**: База художников  
**Технологии**: gRPC, JPA  
**Порт**: 8092 (gRPC)  
**Данные**:
- Биографии
- Стили и направления
- Связи с музеями
- Связи с картинами

### 🖼 Painting Service
**Назначение**: Каталог произведений  
**Технологии**: gRPC, JPA  
**Порт**: 8095 (gRPC)  
**Данные**:
- Метаданные картин
- Связи с художниками
- История экспозиций

### 💻 Frontend
**Назначение**: Пользовательский интерфейс  
**Технологии**: Svelte, TypeScript  
**Порт**: 80 (HTTP)  
**Функции**:
- Адаптивный дизайн
- Оффлайн-режим
- Интерактивные галереи

## Схема взаимодействия

![Схема взаимодействия сервисов Rococo](схема.png)

**Архитектура Rococo** построена на микросервисной модели с четким разделением ответственности между компонентами. Пользователи взаимодействуют с системой исключительно через REST API фронтенда и сервиса аутентификации, что обеспечивает безопасность и простоту интеграции.

Основной шлюз (gateway) выступает единой точкой входа, преобразуя входящие REST-запросы в высокопроизводительные gRPC-вызовы к специализированным сервисам - управления пользователями, музеями, художниками и произведениями искусства.

Для обеспечения слабосвязанности компонентов система использует Kafka, которая обрабатывает асинхронные события, такие как обновление профилей пользователей или изменение коллекций музеев. Это позволяет масштабировать отдельные сервисы независимо и гарантирует отказоустойчивость платформы.


# Минимальные предусловия для работы с проектом Rococo

- На Windows рекомендуется используется терминал bash
- Установить Docker Desktop
- Установить Java версии 21
- Установить пакетный менеджер для сборки front-end npm <br>
  [Инструкция](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm)
- Спуллить контейнер mysql:8.3, zookeeper и kafka версии 7.3.2
```posh
$ docker pull mysql:8.3
$ docker pull confluentinc/cp-zookeeper:7.3.2
$ docker pull confluentinc/cp-kafka:7.3.2
```



После `pull` вы увидите спуленный image командой `docker images`

```posh
mitriis-MacBook-Pro ~ % docker images            
REPOSITORY                 TAG              IMAGE ID       CREATED         SIZE
postgres                   15.1             9f3ec01f884d   10 days ago     379MB
confluentinc/cp-kafka      7.3.2            db97697f6e28   12 months ago   457MB
confluentinc/cp-zookeeper  7.3.2            6fe5551964f5   7 years ago     451MB

```

#### 3. Создать volume для сохранения данных из БД в docker на вашем компьютере

```posh
docker volume create pgdata
```

#### Создать volume для сохранения данных из БД в docker на вашем компьютере
```posh
docker volume create rococo-mysql
```



#### 4. Запустить БД, zookeeper и kafka, фронт скриптоп bash localenv.sh

```posh
User-MacBook-Pro  niffler % bash localenv.sh
```

Фронт будет на порту 3000: http://127.0.0.1:3000/ 

#### 5. Прописать run конфигурацию для всех сервисов rococo-* - Active profiles local

Для этого зайти в меню Run -> Edit Configurations -> выбрать main класс -> в поле Environment variables указать
spring.profiles.active=local

#### 5. Запустить сервис rococo-auth c помощью gradle или командой Run в IDE:

```posh
$ cd rococo-auth
$ gradle bootRun --args='--spring.profiles.active=local'
```

#### 4. Запустить в любой последовательности другие сервисы: rococo-gateway, rococo-userdata, rococo-artist, rococo-geo, rococo-museum, rococo-painting


<a name="запуск_в_docker"></a>
Для запуска в Docker:

### Прописать алиасы в  etc/hosts:

```posh
127.0.0.1 localhost
127.0.0.1 frontend.rococo.dc
127.0.0.1 auth.rococo.dc
127.0.0.1 rococo-all-db
127.0.0.1 gateway.rococo.dc
127.0.0.1 museum.rococo.dc
127.0.0.1 artist.rococo.dc
127.0.0.1 painting.rococo.dc
127.0.0.1 userdata.rococo.dc
127.0.0.1 allure
```

#### Команда для развертывания:

```bash
bash docker-compose.sh
```
#### Команда для развертывания с автотестами:

```bash
bash docker-compose-e2e.sh
```

# Пример тестового отчета
<img src="report.png" alt="Allure Overview">




