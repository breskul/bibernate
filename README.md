![img_1.png](bibernate_image.png)

# BIBERNATE

#### Version 1.0-SNAPSHOT

## Introduction

BIBERNATE is open source ORM for RDBMS databases with strong code quality and excellent test coverage,
which has a lot of features for persisting entities.
It provides simple and understandable annotation mapping, configuration, cache first level, persistence context, 
entity manager, concurrency control etc.
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
- [Entity Mapping](#entity-manager)
    - [@OneToOne](#onetoone)
    - [@OneToMany](#onetomany)
    - [@OneToOne](#onetoone)
 - [Strategy](#strategy)
   - [SEQUENCE](#sequence)
   - [IDENTITY](#identity)
   - [AUTO](#auto)
- [Cascade](#cascade)
  - [ALL](#Strategy-ALL)
  - [PERSIST](#Strategy-PERSIST)
  - [MERGE](#Strategy-MERGE)
  - [REMOVE](#Strategy-REMOVE)
- [Feature list](#feature-list)
    - [Entity Manager](#entity-manager)
        - [persist](#persist)
        - [remove](#remove)
        - [find](#find)
        - [check status](#check-status)
        - [close](#close)
    - [Entity Transaction](#entity-transaction)
        - [Create EntityTransaction](#create-entitytransaction)
        - [Begin new transaction](#begin-new-transaction)
        - [Commit transaction](#commit-transaction)
        - [Rollback transaction](#rollback-transaction)
        - [Check status transaction](#check-status-transaction)
        - [Set up rollback mode](#set-up-rollback-mode)
        - [Get status rollback mode](#get-status-rollback-mode)
    - [First level cache](#first-level-cache)
    
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
The database settings configure using the properties file that should be in the `resources`.
The default filename is `persistence.properties`, but the configuration could be loaded from the specified properties file.

E.g.:
```properties
db.url=jdbc:postgresql://88.10.195.17:5432/postgres
db.user=test
db.password=test
```

Use the `PersistenceProperties.initialize` method to load the configuration.

- Load configuration from the default properties file:
```java
PersistenceProperties.initialize();
```
- Load configuration from the specified properties file:
```java
PersistenceProperties.initialize("test-configuration.properties");
```


### Quick start

```java
import com.breskul.bibernate.configuration.PersistenceProperties;
import com.breskul.bibernate.persistence.EntityManagerImpl;
import com.breskul.bibernate.persistence.test_model.Person;
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
        
        entityManager.close();
    }
}
```


## Technologies
***

| Requirements              | Reason      |
|---------------------------|-------------|
| `Java 17 LTS`           	 | Application |
| `Maven version 3.6.3+` 	  | Builder 	   |

#### Technology stack

| Technology name       | Version   |
|-----------------------|-----------|
| `JDK`        	        | `17 LTS`  |
| `HikariCP`        	   | `5.0.1`   |
| `lombok`        	     | `1.18.24` |
| `log4j-core`        	 | `2.7`     |
| `JUnit`               | `5.9.2`   |

## Entity Mapping
##### @OneToOne
> Specifies a single-valued association to another entity class that has many-to-one multiplicity.
> Has `optional` parameter which shows whether the association is optional. 
> If it set to false then a non-null relationship must always exist.
> His parameter is `true` by default.

##### @OneToMany
> Specifies a many-valued association with one-to-many multiplicity
> Has `fetch` parameter which shoes whether the association should be lazily loaded or must be eagerly fetched. The 
> EAGER strategy is a requirement on the persistence provider runtime that the associated entities must be eagerly fetched. 
> The LAZY strategy is a hint to the persistence provider runtime.
> His parameter is `FetchType.LAZY` by default.

##### @ManyToOne
> Specifies a single-valued association to another entity class that has many-to-one multiplicity
> Has `optional` parameter which shows whether the association is optional.
> If it set to false then a non-null relationship must always exist.
> His parameter is `true` by default.

## Strategy
##### Sequence
>  This strategy uses a database sequence to generate primary keys. The sequence is created in the database and is incremented each time a new row is inserted.
##### Identity
>  The database generates the primary key values as new rows are inserted, and the generated ID is assigned to the entity object.
##### Auto
>  User have to specify his own id for the newly inserted entity

## Cascade
##### Strategy-ALL
>  Indicates that all cascade operations should be performed on the related entity.

##### Strategy-PERSIST
> Indicates that the related entity should be persisted along with the owning entity.

##### Strategy-MERGE
> Indicates that changes made to the related entity should be merged into the owning entity.
> 
##### Strategy-REMOVE
> Indicates that the related entity should be removed along with the owning entity.



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
> If the entity instance is contained in the persistence context, it is returned from there. If a vendor-specific 
> property or hint is not recognized, it is silently ignored.
> ###### <u>Params</u>:
> * entityClass – entity class 
> * primaryKey – primary key properties – standard and vendor-specific properties and hints
> ###### <u>Returns</u>:
> the found entity instance or null if the entity does not exist
> 
> ```java
> Person person = entityManager.find(Person.class, 1L);
> ```

##### Check status
> To check status for EntityManger use this method: 
> ###### <u>Returns</u>:
> return boolean value 
>
> ```java
> entityManager.isOpen();
> ```

##### Close
> Close session and clear all resources
> ```java
> entityManager.close();
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

### First level cache
>Bibernate provide first level cache.
>Find, merge, persist, remove methods will update cache and help avoid additional calls to database.
> ```java
> entityManager.persist(person); - will add value to cache
> ```
> ```java
> entityManager.find(Person.class, 1L); - will add value to cache
> ```
> ```java
> entityManager.remove(person); - will remove value from cache
> ```
> ```java
> entityManager.merge(person); - will update cache value
> ```
>```java
> entityTransaction.rollback(); will clear first level cache
> ```
>```java
> entityManager.close(); will clear first level cache
> ```
### Entity Mapping
To correctly create or map tables following annotations should be used
> * @Entity: This annotation is used to specify that the class is an entity.
> * @Table: This annotation is used to specify the database table that the entity maps to.
> * @Id: This annotation is used to specify the primary key of the entity.
> * @GeneratedValue: This annotation is used to specify how the primary key should be generated.
> * @Column: This annotation is used to specify the mapping between a property and a column in the database table.
>
> Example of a single table
> ```java
> import lombok.Data;
> import lombok.ToString;
> 
> @Entity
> @Data
> @ToString
> @Table(name = "persons")
> public class Person {
>
>	        @Id
>	        @GeneratedValue(strategy = Strategy.IDENTITY)
>	        private Long id;
> 
>	        @Column(name = "first_name")
>	        private String firstName;
> 
>	        @Column(name = "last_name")
>	        private String lastName;
>
>
>	        @Column(name = "birthday")
>	        private LocalDate birthday;
>   }
> ```
> Example of table with one-to-many relation 
> ```java
> import lombok.Data;
> import lombok.ToString;
> 
> @Entity
> @Data
> @ToString
> @Table(name = "persons")
> public class Person {
>
>           @Id
>           @GeneratedValue(strategy = Strategy.IDENTITY)
>           private Long id;
> 
>           @Column(name = "first_name")
>           private String firstName;
> 
>           @Column(name = "last_name")
>           private String lastName;
>
>
>           @Column(name = "birthday")
>           private LocalDate birthday;
> 
>           @OneToMany
>           private List<NoteComplex> notes = new ArrayList<>();
>
>           public void addNote(NoteComplex note) {
>               note.setPerson(this);
>               notes.add(note);
>           }
>           public void removeNote(NoteComplex note) {
>               note.setPerson(null);
>               notes.remove(note);
>           }
>   }
> 
> @Entity
> @Data
> @Table(name = "notes")
> @ToString
> public class NoteComplex {
>
>    @Id
>    @GeneratedValue(strategy = SEQUENCE)
>    private Long id; 
> 
>    @Column(name = "body")
>    private String body;
>
>    @Column(name = "created_at")
>    private LocalDateTime createdAt = LocalDateTime.now();
>
>    @ManyToOne
>    @JoinColumn(name = "person_id")
>    private Person person;
>
> }
> ```
> 

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
