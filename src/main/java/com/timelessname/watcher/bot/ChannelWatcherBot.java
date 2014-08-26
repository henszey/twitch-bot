package com.timelessname.watcher.bot;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.jibble.pircbot.PircBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.timelessname.watcher.domain.AppProperties;
import com.timelessname.watcher.domain.Message;

@Component
@EnableConfigurationProperties(AppProperties.class)
public class ChannelWatcherBot extends PircBot implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(ChannelWatcherBot.class);

  @Autowired
  protected AppProperties config;

  @Autowired
  protected RabbitTemplate rabbitTemplate;
  
  @Autowired
  protected Gson gson;

  protected boolean running = true;

  protected int connectAttempt = 0;
  
  protected Set<String> joinedChannels;

  @Override
  public void onMessage(String rawChannel, String sender, String login, String hostname, String origMessage) {
    String channel = rawChannel.replaceAll("#", "");
    Message message = new Message();
    message.setChannel(channel);
    message.setUser(sender);
    message.setMessage(origMessage);
    String routing = channel + ".message";
    rabbitTemplate.convertAndSend(config.getExchangeName(), routing, gson.toJson(message));
  }

  @Override
  protected void onConnect() {
    joinedChannels = new HashSet<String>();
    logger.info("Connected");
  }

  @Override
  protected void onDisconnect() {
    joinedChannels = null;
    logger.info("Disconnected");
  }

  private void connect() throws Exception {
    String[] addresses = config.getServers();
    String address = addresses[connectAttempt % addresses.length];
    try {
      this.connect(address, 6667, config.getPassword());
    } catch (Exception e) {
      logger.info("Failed to connect to: " + address + " Total attempts: " + connectAttempt);
      connectAttempt++;
      Thread.sleep(5000);
    }
  }

  @Override
  public void run() {
    setName(config.getName());
    try {
      while (running) {
        if (!isConnected()) {
          connect();
        }
        while (isConnected()) {
          joinChannels();
          if(isConnected()){
            Thread.sleep(60000 * 5);
          }
        }
        Thread.sleep(5000);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void joinChannels() {
    try{
      List<String> channels = getChannelsToJoin();
      for (String channel : channels) {
        if (joinedChannels.contains(channel))
          continue;
        Thread.sleep(2500);
        logger.info("Joining #"+joinedChannels.size()+": " + channel);
        joinChannel("#" + channel);
        joinedChannels.add(channel);
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> getChannelsToJoin() throws Exception {
    Thread.sleep(5000);
    List<String> channels = new ArrayList<String>();
    String json = IOUtils.toString(new URL(config.getChannelListUrl()).openStream());
    Map<String, Object> streams = gson.fromJson(json, Map.class);
    for (Map<String, Object> channel : (List<Map<String, Object>>) streams.get("streams")) {
      Map<String, String> data = (Map<String, String>) channel.get("channel");
      String name = data.get("name");
      channels.add(name);
    }
    return channels;
  }
  
  @PreDestroy
  public void close(){
    running = false;
    disconnect();
    //TODO: Interrupt
  }
}
