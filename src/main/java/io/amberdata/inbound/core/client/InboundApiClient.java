package io.amberdata.inbound.core.client;

import io.amberdata.inbound.core.configuration.InboundApiProperties;
import io.amberdata.inbound.core.state.ResourceStateStorage;
import io.amberdata.inbound.domain.BlockchainEntity;

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
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@PropertySource("classpath:inbound-defaults.properties")
@EnableConfigurationProperties(InboundApiProperties.class)
public class InboundApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(InboundApiClient.class);

  private final WebClient            webClient;
  private final InboundApiProperties apiProperties;
  private final ResourceStateStorage stateStorage;

  /**
   * Default constructor.
   *
   * @param apiProperties the properties object for this class
   * @param stateStorage the storage where the state of the entities is saved to and restored from
   */
  public InboundApiClient(InboundApiProperties apiProperties, ResourceStateStorage stateStorage) {
    this.apiProperties = apiProperties;
    this.stateStorage  = stateStorage;

    this.webClient = WebClient.builder()
        .baseUrl(apiProperties.getUrl())
        .defaultHeaders(this::defaultHttpHeaders)
        .build();

    LOG.info("Creating Inbound API client with configured with {}", apiProperties);
  }

  private void defaultHttpHeaders(HttpHeaders httpHeaders) {
    httpHeaders.add("x-amberdata-blockchain-id", this.apiProperties.getBlockchainId());
    httpHeaders.add("x-amberdata-api-key",       this.apiProperties.getApiKey());
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
  }

  /**
   * Publishes the entity to the specified endpoint.
   *
   * @param endpointUri  The endpoint to publish to
   * @param entity the entity to publish
   * @param <T> the type of entity
   *
   * @return the response from the HTTP call
   */
  public <T extends BlockchainEntity> String publish(String endpointUri, T entity) {
    return this.publish(endpointUri, Collections.singletonList(entity));
  }

  /**
   * Publishes the entities to the specified endpoint.
   *
   * @param endpointUri  The endpoint to publish to
   * @param entities the entities to publish
   * @param <T> the type of entity
   *
   * @return the response from the HTTP call
   */
  public <T extends BlockchainEntity> String publish(String endpointUri, List<T> entities) {
    String response = this.webClient
        .post()
        .uri(endpointUri)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromObject(entities))
        .retrieve()
        .bodyToMono(String.class)
        .retryWhen(companion -> companion
            .zipWith(Flux.range(1, Integer.MAX_VALUE), this::handleError)
            .flatMap(this::backOffDelay)
        ).block();

    LOG.info("Server response: {}", response);

    return response;
  }

  /**
   * Publishes the entity to the specified endpoint, and saves it in the pre-defined storage.
   *
   * @param endpointUri  The endpoint to publish to
   * @param entityWithState the entity to publish, with its state
   * @param <T> the type of entity
   *
   * @return the last entity published
   */
  public <T extends BlockchainEntity> BlockchainEntityWithState<T> publishWithState(
      String endpointUri,
      BlockchainEntityWithState<T> entityWithState
  ) {
    return this.publishWithState(endpointUri, Collections.singletonList(entityWithState));
  }

  /**
   * Publishes the entities to the specified endpoint, and saves the last successfully stored entity
   * in the pre-defined storage.
   *
   * @param endpointUri  The endpoint to publish to
   * @param entitiesWithState the entities to publish, with their state
   * @param <T> the type of entity
   *
   * @return the last entity published
   */
  public <T extends BlockchainEntity> BlockchainEntityWithState<T> publishWithState(
      String endpointUri,
      List<BlockchainEntityWithState<T>> entitiesWithState
  ) {
    this.publish(endpointUri, extractEntitiesFrom(entitiesWithState));

    BlockchainEntityWithState<T> lastEntityWithState = entitiesWithState.get(
        entitiesWithState.size() - 1
    );

    this.stateStorage.storeState(lastEntityWithState);

    return lastEntityWithState;
  }

  private <T extends BlockchainEntity> List<T> extractEntitiesFrom(
      List<BlockchainEntityWithState<T>> entitiesWithState
  ) {
    return entitiesWithState.stream()
        .map(BlockchainEntityWithState::getEntity)
        .peek(entity -> LOG.info("Ready for publishing {}", entity))
        .collect(Collectors.toList());
  }

  private int handleError(Throwable error, int retryIndex) {
    LOG.info("Trying to recover after error {} times", retryIndex);

    if (error instanceof WebClientResponseException) {
      WebClientResponseException responseException = (WebClientResponseException) error;
      LOG.info("Server responded with error \n code:{} \n status:'{}' \n body:'{}'",
          responseException.getRawStatusCode(),
          responseException.getStatusText(),
          responseException.getResponseBodyAsString()
      );
    } else {
      LOG.info("Server responded with error", error);
    }

    if (retryIndex < this.apiProperties.getRetriesOnError()) {
      return (int) Math.pow(2, retryIndex);
    }
    throw Exceptions.propagate(error);
  }

  private Mono<Long> backOffDelay(Integer exponentialMultiplier) {
    Duration delayDuration = this.apiProperties
        .getBackOffTimeoutInitial()
        .multipliedBy(exponentialMultiplier);

    if (delayDuration.compareTo(this.apiProperties.getBackOffTimeoutMax()) > 0) {
      delayDuration = this.apiProperties.getBackOffTimeoutMax();
    }
    LOG.info("Back-off delay {}ms", delayDuration.toMillis());

    return Mono.delay(delayDuration);
  }

}
