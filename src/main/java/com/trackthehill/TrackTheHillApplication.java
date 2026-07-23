package com.trackthehill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling
@SpringBootApplication
public class TrackTheHillApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackTheHillApplication.class, args);
	}

}
