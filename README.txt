
NGO Nabarun - DDD demo project (auto-generated)
Modules: ngo-nabarun-domain, ngo-nabarun-application, ngo-nabarun-infra, ngo-nabarun-common, ngo-nabarun-web
GroupId: ngo.nabarun, Version: 1.0.0

How to run (from project root):
  mvn -pl ngo-nabarun-web spring-boot:run

Notes:
- The infrastructure uses Spring Data MongoDB. Start a MongoDB instance locally (default URI in web/src/main/resources/application.properties).
- Domain contains the Member aggregate and repository interface.
- Infra contains a Spring Data repository and an adapter that implements the domain repository.
- Application contains service and DTO/mapper used by web controllers.
