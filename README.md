![img_1.png](bibernate_image.png)

# BIBERNATE

#### Version 1.0-SNAPSHOT

## Introduction

BIBERNATE is open source ORM for RDBMS databases with strong code quality and excellent test coverage,
which has a lot of features for persisting entities.
It provides simple and understandable annotation mapping, configuration, cache first level, persistence context, entity manager, concurrency control etc.
The list of all features with code examples you can find in this guide.

#### Developed by BRESKUL team in bound of educational program https://www.bobocode.com/

## Table of Contents
<!-- TOC depthFrom:1 depthTo:3 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Installation](#installation)
    - [Apache Maven](#apache-maven)
    - [Settings](#settings)
      - [Database settings](#database-settings)
    - [Quick start](#quick-start)
- [Technologies](#technologies)
    - [Technology stack](#technology-stack)
- [Feature list](#feature-list)
    - [Entity Manager](#entity-manager)
        - [persist](#persist)
        - [remove](#remove)
        - [find](#find)
    - [Entity Transaction](#entity-transaction)
        - [Create EntityTransaction](#create-entitytransaction)
        - [Begin new transaction](#begin-new-transaction)
        - [Commit transaction](#commit-transaction)
        - [Rollback transaction](#rollback-transaction)
        - [Check status transaction](#check-status-transaction)
        - [Set up rollback mode](#set-up-rollback-mode)
        - [Get status rollback mode](#get-status-rollback-mode)
    
<!-- /TOC -->


## Installation
***

##### Apache Maven
If you’re using Maven to build your project add the following to your pom.xml to use BIBERNATE:
```$xslt
<dependency>
    <groupId>com.breskul.bibernate</groupId>
    <artifactId>bibernate</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Settings
##### Database settings
The database settings should be input in `resources` folder in `persistence.properties` file

E.g.:
```properties
db.url=jdbc:postgresql://88.10.195.17:5432/postgres
db.user=test
db.password=test
```

### Quick start

```java
import com.breskul.bibernate.configuration.PersistenceProperties;
import com.breskul.bibernate.persistence.EntityManagerImpl;
import com.breskul.bibernate.persistence.testmodel.Person;
import com.breskul.bibernate.repository.DataSourceFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.Month;

public class DemoApp {
    public static void main(String[] args) {
        PersistenceProperties.initialize();
        DataSourceFactory dataSourceFactory = DataSourceFactory.getInstance();
        DataSource dataSource = dataSourceFactory.getDataSource();
        EntityManager entityManager = new EntityManagerImpl(dataSource);
        EntityTransaction entityTransaction = entityManager.getTransaction();
        
        entityTransaction.begin();

        Person person = new Person();
        person.setFirstName("Keanu");
        person.setLastName("Reeves");
        person.setBirthday(LocalDate.of(1964, Month.SEPTEMBER, 2));

        entityManager.persist(person);
        Person selectedPerson = entityManager.find(Person.class, 1L);
        System.out.println(selectedPerson);
        entityManager.remove(selectedPerson);
        
        entityTransaction.commit();
    }
}
```


## Technologies
***

  Requirements                |  Reason           
------------------------------|------------------------------
 `Java 17 LTS`           	  |  Application              
 `Maven version 3.6.3+` 	  |  Builder 	                 

#### Technology stack

  Technology name             |  Version
------------------------------|------------------------------
  `JDK`        	              |  `17 LTS`                           
  `HikariCP`        	      |  `5.0.1`                            
  `lombok`        	          |  `1.18.24`                          
  `log4j-core`        	      |  `2.7`


## Feature list
***
There are many features implemented in the project. All features are tested.

### Entity Manager

##### persist
> Make an instance managed and persistent.
> ###### <u>Params</u>:
> * entity – entity instance
> ```java
> Person person = new Person();
> person.setFirstName("Keanu");
> person.setLastName("Reeves");
> person.setBirthday(LocalDate.of(1964, Month.SEPTEMBER, 2));
> entityManager.persist(person);
> ```

##### remove
> Remove the entity instance.
> ###### <u>Params</u>:
> * entity – entity instance
> ```java
> entityManager.remove(person);
> ```

##### find
> Find by `primary key`, using the specified properties. Search for an entity of the specified class and primary key. 
> If the entity instance is contained in the persistence context, it is returned from there. If a vendor-specific property or hint is not recognized, it is silently ignored.
> ###### <u>Params</u>:
> * entityClass – entity class 
> * primaryKey – primary key properties – standard and vendor-specific properties and hints
> ###### <u>Returns</u>:
> the found entity instance or null if the entity does not exist
> 
> ```java
> Person person = entityManager.find(Person.class, 1L);
> ```

### Entity Transaction

Hibernate provide transaction mechanism.
Before use persistence operations need create EntityTransaction and open new transaction
and after operations need to make commit or rollback transaction

##### Create EntityTransaction
>```java
> EntityTransaction entityTransaction = entityManager.getTransaction();
> ```

##### Begin new transaction
>```java
> entityTransaction.begin();
> ```

##### Commit transaction
>```java
> entityTransaction.commit();
> ```

##### Rollback transaction
>```java
> entityTransaction.rollback();
> ```

##### Check status transaction
>```java
> entityTransaction.isActive();
> ```

##### Set up rollback mode
>Set up rollback only mode will to do rollback for commit too
> ```java
> entityTransaction.setRollbackOnly();
> ```

##### Get status rollback mode
> ```java
> entityTransaction.getRollbackOnly();
> ```

## Our BRESKUL Team
***
* Andrii Tsepukh - Team Mentor
* Mihail Mihailenko - Team Lead
* Yuriy Fomin
* Gleb Nogai
* Ihor Neshyk
* Serhii Sewerin
* Serhii Yevtushok
* Oleg Shiriaev
* Artem Yankovets

## Our MASTER
***
* Taras Boychuk - Java Ultimate Program Lead at Bobocode. 
  <br>email: tboychuk@bobocode.com
  <br>https://www.bobocode.com/
