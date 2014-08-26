package com.timelessname.watcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.timelessname.watcher.bot.ChannelWatcherBot;

@Configuration
public class BotConfig {

  @Bean(initMethod="start")
  public Thread botThread(ChannelWatcherBot bot){
    return new Thread(bot);
  }
  
  @Bean
  public Gson gson(){
    return new Gson();
  }
  
}
