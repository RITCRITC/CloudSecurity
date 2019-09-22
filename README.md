Cloud Security
============

This repository contains cloud security projects with [Spring Boot](https://projects.spring.io/spring-boot), [Spring Cloud Config](https://cloud.spring.io/spring-cloud-config/) and [Vault](https://www.vaultproject.io). It offers different possibilities on how to store secrets securely for local and cloud based web applications.

Every application (clients and config servers) exposes all Spring Actuator endpoints at the default */actuator* endpoint.

# Requirements
- [Java 11](http://www.oracle.com/technetwork/java)
- [Lombok](https://projectlombok.org/)
- [Maven 3](http://maven.apache.org/)

# Jasypt

## local-client
The local client application is using [Jasypt for Spring Boot](https://github.com/ulisesbocchio/jasypt-spring-boot) to secure sensitive configuration properties. This demo application shows the most simple way to encrypt sensitive properties without requiring another web application or any external system. You have to provide an environment variable named `jasypt.encryptor.password` with the value `sample-password` to decrypt the database password during application start.  After launching, `http://localhost:8080` shows basic application information, other entities are exposed via Spring Data Rest at the `/credentials` and `/users` endpoints.

# Spring Cloud Config
All client applications use [Spring Cloud Config](https://cloud.spring.io/spring-cloud-config/) to separate code and configuration and therefore require a running config server before starting the actual application.

## config-server
This project contains the Spring Cloud Config server which must be started like a Spring Boot application before using the **config-client** web application. After starting the config server without a specific profile, the server is available on port 8888 and will use the configuration files provided in the **config-repo** folder in my GitHub repository.

Starting the config server without a profile therefore requires Internet access to read the configuration files from my GitHub repo. To use a local configuration instead (e.g. the one in the **config-repo** directory) you have to enable the **native** profile during startup and to provide a file system resource location containing the configuration, e.g. 

    spring.cloud.config.server.native.search-locations=file:/var/config-repo/

The basic auth credentials (user/secret) are required when accessing the config server.

### config-repo
This folder contains all configuration files for all profiles used in the **config-client** and **config-client-vault** applications.

## config-client
This Spring Boot based web application exposes the REST endpoints `/`, `/users` and `/credentials`. Depending on the active Spring profile, the configuration files used are not encrypted (**plain**) or secured using Spring Config encryption functionality (**cipher**). There is no default profile available so you have to provide a specific profile during start.

### Profile plain
Configuration files are not protected at all, even sensitive configuration properties are stored in plain text.

### Profile cipher
This profile uses Config Server functionality to encrypt sensitive properties. It requires either a symmetric or asymmetric key. The sample is based on asymmetric encryption and is using a keystore (`server.p12`) which was created with the following command:

    keytool -genkeypair -alias configserver -storetype PKCS12 -keyalg RSA \
      -dname "CN=Config Server,OU=Unit,O=Organization,L=City,S=State,C=Germany" \
      -keypass secret -keystore server.p12 -storepass secret
      
The Config Server endpoints help to encrypt and decrypt data:

    curl localhost:8888/encrypt -d secretToEncrypt -u user:secret
    curl localhost:8888/decrypt -d secretToDecrypt -u user:secret

# Vault
A local [Vault](https://www.vaultproject.io/) server is required for the **config-client-vault** and the **config-server-vault** applications to work. Using Docker as described below is the recommended and fully initialized version.

## Docker
Switch to the Docker directory in this repository and execute `docker-compose up -d`. This will launch a preconfigured Vault container which already contains all required configuration for the demo applications. The only thing you have to do is to unseal Vault with the master key provided [here](https://github.com/dschadow/CloudSecurity/blob/develop/Docker/vault-keys.json) (key-shares and key-threshold are both set to 1). The easiest way to do that is to open Vault web UI in your browser (http://localhost:8200/ui). After that, you can start the Spring Boot applications as described below.

## Local Installation
Vault must be started on localhost with the [in-memory configuration](https://github.com/dschadow/CloudSecurity/blob/develop/vault-inmem.conf) in the projects' root directory:

    vault server -config vault-inmem.conf
    export VAULT_ADDR=http://127.0.0.1:8200
    vault operator init -key-shares=5 -key-threshold=2
    export VAULT_TOKEN=[Root Token]
    vault operator unseal [Key 1]
    vault operator unseal [Key 2]
    
There are two more configuration files in his directory: [file configuration](https://github.com/dschadow/CloudSecurity/blob/develop/vault-file.conf) which stores all Vault data in the configured directory and [consul configuration](https://github.com/dschadow/CloudSecurity/blob/develop/vault-consul.conf) which uses Consul for that purpose (a running Consul must be available).

The displayed root token must be available for every Spring application that wants to access vault. Alternatively, it is possible to start the Vault server locally in dev mode and provide the configured root-token-id during initialization (recommended for first steps):

    vault server -dev -dev-root-token-id="00000000-0000-0000-0000-000000000000" -dev-listen-address="127.0.0.1:8200"  
    export VAULT_DEV_ROOT_TOKEN_ID=[Root Token]
    export VAULT_ADDR=http://127.0.0.1:8200  

The created Vault must contain the following values that are not contained in the Spring Cloud Config configuration for **config-client-vault**:

    vault kv put secret/config-client-vault application.name="Config Client Vault" application.profile="Demo"
    
### Transit Engine
Further configuration is required to interact with the **transit** endpoints of the config-client-vault application:

    vault secrets enable transit
    vault write -f transit/keys/my-sample-key
    
Now you can use the **transit** endpoints with the key name **my-sample-key**.

## config-server-vault
This project contains the Spring Cloud Config server which must be started like a Spring Boot application before using the **config-client-vault** web application. After starting the config server without a specific profile, the server is available on port 8888 and will use the configuration provided in the given Vault. The [bootstrap.yml](https://github.com/dschadow/CloudSecurity/blob/develop/config-server-vault/src/main/resources/bootstrap.yml) requires a valid Vault token: this is already set for the Vault Docker container but must be updated in case you are using your own Vault. Clients that want to access any configuration must provide a valid Vault token as well via a *X-Config-Token* header.

## config-client-vault
This Spring Boot based web application contacts the Spring Cloud Config Server for configuration and exposes the REST endpoints `/`, `/users`, `/credentials` (like the **config-client** application) and `/secrets`. The `/secrets` endpoint communicates with Vault directly and provides POST and GET methods to read and write individual values to the configured Vault. You can use the applications **Swagger UI** on `http://localhost:8080/swagger-ui.html` to interact with all endpoints.
    
The [bootstrap.yml](https://github.com/dschadow/CloudSecurity/blob/develop/config-client-vault/src/main/resources/bootstrap.yml) file in the **config-client-vault** project does require the root token: this is already set for the Vault Docker container but must be updated in case you are using your own Vault.

## Meta
[![Build Status](https://travis-ci.org/dschadow/CloudSecurity.svg)](https://travis-ci.org/dschadow/CloudSecurity)
[![codecov](https://codecov.io/gh/dschadow/CloudSecurity/branch/develop/graph/badge.svg)](https://codecov.io/gh/dschadow/CloudSecurity)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
