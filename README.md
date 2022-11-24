# user management rest api

## stack & features
- java
- spring
- spring security with dynamic access restriction for URLs
- jwt access token & refresh token linked to users device
- RSA keys generator
- hibernate
- postgreSQL
- flyway
- redis & jedis for blacklisting access tokens after logout and locking users after number of failed login attempts
- kafka for integration with email notification service
- lombok
- mapstruct
- hateoas
- customized hibernate envers for audit
- custom spring events
- custom spring validation
- robohash image generator api & file cache
- supports xml, json and files for profile images
- supports en, cs languages
- spring profiles
  - local - h2, embedded redis, embedded kafka
  - dev - docker-compose, createdb, initdb and flyway scripts
- postman collection in root folder

## TODOs
- API versioning
- prevent roles from cycling themselves
- prevent manipulating with roles and authorities which the user has not
- DB independent tech user?
- JUnit and integration tests

### &check; angular frontend in progress
