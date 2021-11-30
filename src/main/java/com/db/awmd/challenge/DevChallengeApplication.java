package com.db.awmd.challenge;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
public class DevChallengeApplication {

  public static void main(String[] args) {
    SpringApplication.run(DevChallengeApplication.class, args);
  }
  
  @Bean(name = "notificationService")
  public Executor notificationTaskExecutor() {
	  ThreadPoolTaskExecutor treadPoolTaskExecutor = new ThreadPoolTaskExecutor();
	  treadPoolTaskExecutor.setQueueCapacity(1000);
	  treadPoolTaskExecutor.setCorePoolSize(10);
	  treadPoolTaskExecutor.setMaxPoolSize(25);
	  return treadPoolTaskExecutor;
  }
}
