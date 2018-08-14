package io.amberdata.ingestion.core.state.repositories;

import io.amberdata.ingestion.core.state.entities.ResourceState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceStateRepository extends JpaRepository<ResourceState, String> {

}
