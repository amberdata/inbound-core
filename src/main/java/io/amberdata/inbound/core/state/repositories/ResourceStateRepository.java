package io.amberdata.inbound.core.state.repositories;

import io.amberdata.inbound.core.state.entities.ResourceState;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceStateRepository extends JpaRepository<ResourceState, String> {
}
