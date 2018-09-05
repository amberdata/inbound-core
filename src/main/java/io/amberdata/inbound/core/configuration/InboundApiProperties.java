package io.amberdata.inbound.core.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties("inbound.api")
public class InboundApiProperties {
  private String url;
  private String blockchainId;
  private String apiKey;

  private Integer retriesOnError;

  @DurationUnit(ChronoUnit.MILLIS)
  private Duration backOffTimeoutInitial;
  @DurationUnit(ChronoUnit.MILLIS)
  private Duration backOffTimeoutMax;

  public String getUrl() {
    return this.url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getBlockchainId() {
    return this.blockchainId;
  }

  public void setBlockchainId(String blockchainId) {
    this.blockchainId = blockchainId;
  }

  public String getApiKey() {
    return this.apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public Integer getRetriesOnError() {
    return this.retriesOnError > 0 ? this.retriesOnError : Integer.MAX_VALUE;
  }

  public void setRetriesOnError(Integer retriesOnError) {
    this.retriesOnError = retriesOnError;
  }

  public Duration getBackOffTimeoutInitial() {
    return this.backOffTimeoutInitial;
  }

  public void setBackOffTimeoutInitial(Duration backOffTimeoutInitial) {
    this.backOffTimeoutInitial = backOffTimeoutInitial;
  }

  public Duration getBackOffTimeoutMax() {
    return this.backOffTimeoutMax;
  }

  public void setBackOffTimeoutMax(Duration backOffTimeoutMax) {
    this.backOffTimeoutMax = backOffTimeoutMax;
  }

  @Override
  public String toString() {
    return
        "InboundApiProperties{"
        + "url='" + this.url + '\''
        + ", blockchainId='" + this.blockchainId + '\''
        + ", apiKey='" + this.apiKey + '\''
        + ", retriesOnError=" + this.retriesOnError
        + ", backOffTimeoutInitial=" + this.backOffTimeoutInitial
        + ", backOffTimeoutMax=" + this.backOffTimeoutMax
        + '}';
  }
}
