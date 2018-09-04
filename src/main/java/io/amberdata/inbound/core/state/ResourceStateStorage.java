package io.amberdata.inbound.core.state;

import io.amberdata.inbound.core.client.BlockchainEntityWithState;
import io.amberdata.inbound.core.state.entities.ResourceState;
import io.amberdata.inbound.core.state.repositories.ResourceStateRepository;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

@Component
@EnableJpaRepositories("io.amberdata.inbound.core.state.repositories")
@EntityScan("io.amberdata.inbound.core.state.entities")
public class ResourceStateStorage {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceStateStorage.class);

  private final ResourceStateRepository resourceStateRepository;

  public ResourceStateStorage(ResourceStateRepository resourceStateRepository) {
    this.resourceStateRepository = resourceStateRepository;

    resourceStateRepository.findAll().forEach(this::logStateRecord);
  }

  private void logStateRecord(ResourceState resourceState) {
    LOG.info("Stored state record: [ {} | {} ]", resourceState.getResourceType(),
        resourceState.getStateToken());
  }

  public void storeState(BlockchainEntityWithState entityWithState) {
    ResourceState resourceState = entityWithState.getResourceState();

    LOG.info("Going to store state for {} with state token {}",
        resourceState.getResourceType(),
        resourceState.getStateToken()
    );

    this.resourceStateRepository.saveAndFlush(
        ResourceState.from(
            resourceState.getResourceType(),
            resourceState.getStateToken()
        )
    );
  }

  public String getStateToken(String resourceType, Supplier<String> defaultStateTokenSupplier) {
    return this.resourceStateRepository
        .findById(resourceType)
        .map(ResourceState::getStateToken)
        .orElse(defaultStateTokenSupplier.get());
  }
}
