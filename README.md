# Java Shareit (API)

<a href="https://www.java.com" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original.svg" alt="java" width="40" height="40"/> </a>  <a href="https://www.postgresql.org" target="_blank" rel="noreferrer"> <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/postgresql/postgresql-original-wordmark.svg" alt="postgresql" width="40" height="40"/> </a> <a href="https://postman.com" target="_blank" rel="noreferrer"> <img src="https://www.vectorlogo.zone/logos/getpostman/getpostman-icon.svg" alt="postman" width="40" height="40"/> </a> <a href="https://spring.io/" target="_blank" rel="noreferrer"> <img src="https://www.vectorlogo.zone/logos/springio/springio-icon.svg" alt="spring" width="40" height="40"/> </a>

<p align="justify">Shareit - это двухмодульное приложение-микросервис для размещения и поиска вещей пользователей, которыми они готовы поделитсья.</p>

## Идея

<p align="justify">Почему шеринг так популярен. Представьте, что на воскресной ярмарке вы купили несколько картин и хотите повесить их дома. Но вот незадача — для этого нужна дрель, а её у вас нет. Можно, конечно, пойти в магазин и купить, но в такой покупке мало смысла — после того, как вы повесите картины, дрель будет просто пылиться в шкафу. Можно пригласить мастера — но за его услуги придётся заплатить. И тут вы вспоминаете, что видели дрель у друга. Сама собой напрашивается идея — одолжить её.</p>


## Стек технологий

<p align="justify">Java, Spring, Spring Boot, Maven, Lombook, PostgreSQL, Docker, Hibernate.</p>

## База данных

![image](https://user-images.githubusercontent.com/92802270/221555797-ec926052-3b77-41f9-957d-55a7a0cbd04a.png)
## Запуск
Все логика работает на http://localhost:9090
Для проверки работоспособности можно:
1. использовать postman тесты в корневой папке `sprint.json`
2. Сначала отправить POST запрос с телом `{
   "name": "user",
   "email": "user@user.com"
   }
   `<p>Затем отправить GET запрос на  `http://localhost:9090/users
   `</p>
- Открыть проект в IntelliJ Idea, запустить конфигурацию **RunAllModules**
- Докером (требуется установленный docker и docker-compose):
    1. В виндовсе из корневой папки проекта `build.bat`
    
