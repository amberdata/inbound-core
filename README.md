# Amberdata Ingestion API

The Amberdata Ingestion Domain Model defines the different entities and object models that 3rd parties can use to push data into the Amberdata platform.

After working with and researching a few different blockchains (Ethereum, Aion, Stellar, etc)  we have found some commonalities (and differences!) between them, and this project is the result.

Once ingested into the Amberdata platform, these entities are processed by our backend pipeline, where we combine these data sources with off-chain data, extract metrics, and provide insights, analytics and monitoring into your blockchain.

A full Swagger documentation of the REST API is available [here](https://blockchains.amberdata.io/api/v1/spec).

# Ingestion API domain model

[Ingestion API domain model](https://github.com/amberdata/ingestion-domain-model/blob/master/README.md#getting-started) contains a collection of generic entities 

# Ingestion API Core

`ingestion-core` module is a set of utilities which simplify dealing with the Ingestion API. 
If your application is a SpringBoot 2.x application, 
you may stop worrying about shaping a valid request to the [REST API](https://blockchains.amberdata.io/api/v1/spec),
re-sending requests on error or even having a basic state storage to track which metrics have already be sent.

To solve these routines, you use `IngestionApiClient` java class to publish metrics instead of dealing with your favorite HTTP client directly.
See more details in [Getting Started](#getting-started) section.

# How to use it (Gradle or Maven)

> Here should be instructions of how to add maven repository which contains ingestion-domain-model artifact

Add a dependency to your `pom.xml` of your SpringBoot application

```xml
<dependency>
  <groupId>io.amberdata.ingestion</groupId>
  <artifactId>ingestion-core</artifactId>
  <version>0.0.4</version>
</dependency>

```

or to your `build.gradle`

```gradle
dependencies {
    implementation 'io.amberdata.ingestion:ingestion-core:0.0.4'
}
```

# Build locally

```sh
$ git clone https://github.com/amberdata/ingestion-core.git
$ cd ingestion-core
$ mvn clean install
```

# Before you start

You can get instance of `IngestionApiClient` as spring framework component, however to use it you will need to provide some configuration.

These three properties are essential to let you publish metrics to the Ingestion API. 

> Note that you have to have register your blockhain before you start publishing metrics. To do that, follow the instructions: *put it here*

```properties
ingestion.api.url=https://blockchains.amberdata.io/api/v1
ingestion.api.blockchain-id=CHANGE_ME
ingestion.api.api-key=CHANGE_ME
```

These configuration properties could be passed as application parameters when running your ingestion module. 

```bash
$ java -jar app.jar \
  --ingestion.api.url=https://blockchains.amberdata.io/api/v1 \
  --ingestion.api.blockchain-id=CHANGE_ME
  --ingestion.api.api-key=CHANGE_ME
```

#### Application configuration

To help your SpringBoot application find `IngestionApiClient` class, you need to specify the package.
The simplest way to do so is referring to io.amberdata.ingestion.core.IngestionCore marker interface in` @ComponentScan` annotation

```java 
@SpringBootApplication
@ComponentScan(basePackageClasses = IngestionCore.class)
public class IngestionModuleDemoApplication {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
```

This will let you to inject configured `IngestionApiClient` instance as dependency. 

```java 
@Component
public class BlocksPublisher {
  private final IngestionApiClient ingestionApiClient;
  
  @Autowired
  public BlocksPublisher(IngestionApiClient ingestionApiClient) {
    this.ingestionApiClient = ingestionApiClient;
  }
}
```


To publish blockchain metrics you will run `publish` method specifying the context as in example below

```java 
ingestionApiClient.publish("/blocks", entities); // sends a list of block entities to the ingestion API endpoint 
```

Note than you can send an entity (e.g. block) or a list of entities. 
In each case you will have to wrap entity instance into `BlockchainEntityWithState` which also contains information about state

```java 
Block block = new Block.Builder().number(12345L).build();

BlockchainEntityWithState blockWithState = BlockchainEntityWithState.from(block, ResourceState.from("Block", "12345");

ingestionApiClient.publish("/blocks", blockWithState);
```

The code above will publish the block to the Ingestion API and will store it's number to the internal storage. 

Talking about objects you need to create for publishing a blockchain entity, there is `ResourceState` to highlight.

> Resource type (1st parameter in `ResourceState.from("Block", "12345")`) can be any string which you can use it to identify the type of entity later  
> You can pass any token (2nd parameter in `ResourceState.from("Block", "12345")`) which you can use as entity identity in order to make it possible to recover information from which entity 

##### Working with State Storage

Whenever you need information about the last published blockchain entity (e.g. block) you will get it from the internal storage.
You will use for that an instance of `io.amberdata.ingestion.core.state.ResourceStateStorage` component, which you have injected into your SpringBoot application.

```java 
@Component
public class BlocksPublisher {
  private final IngestionApiClient ingestionApiClient;
  private final ResourceStateStorage stateStorage;
  
  @Autowired
  public BlocksPublisher(IngestionApiClient ingestionApiClient, ResourceStateStorage stateStorage) {
    this.ingestionApiClient = ingestionApiClient;
    this.stateStorage = stateStorage;
  }
}
```

Assuming you already have state info stored in the internal storage (say, you already successfully published some entities to the Ingestion API)
you can get the state calling `getStateToken` method which returns the state's token you previously created with `ResourceState.from`;

```java 
Block block = new Block.Builder().number(12345L).build();

BlockchainEntityWithState blockWithState = BlockchainEntityWithState.from(block, ResourceState.from("Block", 12345L);

ingestionApiClient.publish("/blocks", blockWithState);

stateStorage.getStateToken("Block", () -> "token default value"); 
```

> Note, that if no token found for the key (resourceType) you are looking for, 
the supplier, which you pass as the second parameter, will be invoked and it's value will become the method's return value;

# Implementation example

As an example of how to build you ingestion module using `ingestion-core`, you could have a look at [ingestion API module](https://github.com/amberdata/stellar-ingestion-api-module) which we created for [Stellar](https://www.stellar.org)  

# Licensing

This project is licensed under the [Apache Licence 2.0](./LICENSE).

See also [Contributing](./CONTRIBUTING.md)
