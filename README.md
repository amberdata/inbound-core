## Amberdata Ingestion API

Not every blockchain has exactly the same entities in its domain model... On the other hand mostly all of them have commonalities. If not same names but similar meaning. 

The idea behind the Amberdata Ingestion API domain model is to gather all similarities 
into generic entities which represent blockchain metrics and bridge them to the [REST API](https://blockchains.amberdata.io/api/v1/spec)


### Ingestion API domain model

[Ingestion API domain model](https://github.com/amberdata/ingestion-domain-model/blob/master/README.md#getting-started) contains a collection of generic entities 

### Ingestion API Core

`ingestion-core` module is a set of utilities which simplify dealing with the Ingestion API. 
If your application is a SpringBoot 2.x application, 
you may stop worrying about shaping a valid request to the [REST API](https://blockchains.amberdata.io/api/v1/spec),
re-sending requests on error or even having a basic state storage to track which metrics have already be sent.

To solve these routines, you use `IngestionApiClient` java class to publish metrics instead of dealing with your favorite HTTP client directly.
See more details in [Getting Started](#getting-started) section.

### Getting it

#### With Gradle or Maven

> here should be instructions of how to add maven repository which contains ingestion-core artifact

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

### Getting Started

Here will be some instructions


### Implementation example

As an example of how to build you ingestion module using `ingestion-core`, you could have a look at [ingestion API module](https://github.com/amberdata/stellar-ingestion-api-module) which we created for [Stellar](https://www.stellar.org)  
