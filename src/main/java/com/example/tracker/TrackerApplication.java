package com.example.tracker;

import com.example.tracker.model.User;
import com.example.tracker.model.enums.Role;
import com.example.tracker.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class TrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackerApplication.class, args);
	}

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	public CommandLineRunner initializeDefaultUser(UserRepository userRepository) {
		return args -> {
			if (userRepository.findByUsername("staff") == null) {
				User staffUser = new User();
				staffUser.setUsername("staff");
				staffUser.setRole(Role.CLINICIAN);
				userRepository.save(staffUser);
			}
		};
	}

}
