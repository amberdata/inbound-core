package io.amberdata.ingestion.core.state.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ResourceState {

  @Id
  private String resourceType;

  @Column(nullable = false)
  private String stateToken;

  protected ResourceState() {
  }

  private ResourceState(String resourceType, String stateToken) {
    this.resourceType = resourceType;
    this.stateToken = stateToken;
  }

  public static ResourceState from(String resourceType, String stateToken) {
    return new ResourceState(resourceType, stateToken);
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getStateToken() {
    return stateToken;
  }

  public void setStateToken(String stateToken) {
    this.stateToken = stateToken;
  }

  @Override
  public String toString() {
    return "ResourceState{" +
        "resourceType='" + resourceType + '\'' +
        ", stateToken='" + stateToken + '\'' +
        '}';
  }
}
