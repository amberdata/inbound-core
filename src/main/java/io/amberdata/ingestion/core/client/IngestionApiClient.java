package io.amberdata.ingestion.core.client;

import io.amberdata.ingestion.core.configuration.IngestionApiProperties;
import io.amberdata.ingestion.domain.BlockchainEntity;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@PropertySource("classpath:ingestion-defaults.properties")
@EnableConfigurationProperties(IngestionApiProperties.class)
public class IngestionApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(IngestionApiClient.class);

  private final WebClient webClient;
  private final IngestionApiProperties apiProperties;

  public IngestionApiClient(IngestionApiProperties apiProperties) {
    this.apiProperties = apiProperties;
    this.webClient = WebClient.builder()
        .baseUrl(apiProperties.getUrl())
        .defaultHeaders(this::defaultHttpHeaders)
        .build();

    LOG.info("Creating Ingestion API client with configured with {}", apiProperties);
  }

  private void defaultHttpHeaders(HttpHeaders httpHeaders) {
    httpHeaders.add("x-amberdata-blockchain-id", apiProperties.getBlockchainId());
    httpHeaders.add("x-amberdata-api-key", apiProperties.getApiKey());
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
  }

  public <T extends BlockchainEntity> BlockchainEntityWithState<T> publish(String endpointUri,
      List<BlockchainEntityWithState<T>> entitiesWithState,
      Class<T> entityClass) {

    List<T> entities = entitiesWithState.stream()
        .map(BlockchainEntityWithState::getEntity)
        .peek(entity -> LOG
            .info("Ready for publishing to the ingestion API endpoint {}: {}", endpointUri, entity))
        .collect(Collectors.toList());

    webClient
        .post()
        .uri(endpointUri)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromObject(entities))
        .retrieve()
        .bodyToFlux(entityClass)
        .retryWhen(companion -> companion
            .doOnNext(throwable -> LOG.error("Error occurred: {}", throwable.getMessage()))
            .zipWith(Flux.range(1, Integer.MAX_VALUE), this::handleError)
            .flatMap(this::backOffDelay)
        ).blockLast();

    return entitiesWithState.get(entitiesWithState.size() - 1);
  }

  private int handleError(Throwable error, int retryIndex) {
    LOG.info("Trying to recover after {}: {} times", error.getMessage(), retryIndex);

    if (retryIndex < apiProperties.getRetriesOnError()) {
      return (int) Math.pow(2, retryIndex);
    }
    throw Exceptions.propagate(error);
  }

  private Mono<Long> backOffDelay(Integer exponentialMultiplier) {
    Duration delayDuration = apiProperties.getBackOffTimeoutInitial().multipliedBy(exponentialMultiplier);
    if (delayDuration.compareTo(apiProperties.getBackOffTimeoutMax()) > 0) {
      delayDuration = apiProperties.getBackOffTimeoutMax();
    }
    LOG.info("Back-off delay {}ms", delayDuration.toMillis());

    return Mono.delay(delayDuration);
  }

  public <T extends BlockchainEntity> BlockchainEntityWithState<T> publish(String endpointUri,
      BlockchainEntityWithState<T> entityWithState,
      Class<T> entityClass) {

    return publish(endpointUri, Collections.singletonList(entityWithState), entityClass);
  }
}
