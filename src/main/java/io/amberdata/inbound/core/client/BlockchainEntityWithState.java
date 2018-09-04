package io.amberdata.inbound.core.client;

import io.amberdata.inbound.core.state.entities.ResourceState;
import io.amberdata.inbound.domain.BlockchainEntity;

public final class BlockchainEntityWithState<T extends BlockchainEntity> {
  private final ResourceState resourceState;
  private final T entity;

  private BlockchainEntityWithState(T entity, ResourceState resourceState) {
    this.resourceState = resourceState;
    this.entity = entity;
  }

  public static <T extends BlockchainEntity> BlockchainEntityWithState<T> from(
      T entity,
      ResourceState resourceState
  ) {
    return new BlockchainEntityWithState<>(entity, resourceState);
  }

  public ResourceState getResourceState() {
    return this.resourceState;
  }

  public T getEntity() {
    return this.entity;
  }

  @Override
  public String toString() {
    return "BlockchainEntityWithState{" +
        "resourceState=" + this.resourceState +
        ", entity=" + this.entity +
        '}';
  }
}
