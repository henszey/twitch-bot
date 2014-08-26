package com.timelessname.watcher.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("twitch")
public class AppProperties {

  protected String exchangeName;

  protected String[] servers;

  protected String name;

  protected String password;

  protected String channelListUrl;

  public String getExchangeName() {
    return exchangeName;
  }

  public void setExchangeName(String exchangeName) {
    this.exchangeName = exchangeName;
  }

  public String[] getServers() {
    return servers;
  }

  public void setServers(String[] servers) {
    this.servers = servers;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getChannelListUrl() {
    return channelListUrl;
  }

  public void setChannelListUrl(String channelListUrl) {
    this.channelListUrl = channelListUrl;
  }

}
