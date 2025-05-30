package com.ogulcan.dailymetrics;

import com.ogulcan.dailymetrics.model.ActivityType;
import com.ogulcan.dailymetrics.model.User;
import com.ogulcan.dailymetrics.repository.ActivityTypeRepository;
import com.ogulcan.dailymetrics.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.SecureRandom;

@SpringBootApplication
public class DailyMetricsApplication {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ActivityTypeRepository activityTypeRepository;

	public static void main(String[] args) {
		SpringApplication.run(DailyMetricsApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// Varsayılan admin
		if (userRepository.findByUsername("admin").isEmpty()) {
			User admin = new User();
			admin.setUsername("admin");
			admin.setLoginCode(generateLoginCode());
			admin.setAdmin(true);
			userRepository.save(admin);
			System.out.println("Admin created with login code: " + admin.getLoginCode());
		}
	}

	private String generateLoginCode() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		SecureRandom random = new SecureRandom();
		StringBuilder code = new StringBuilder(16);
		for (int i = 0; i < 16; i++) {
			code.append(characters.charAt(random.nextInt(characters.length())));
		}
		return code.toString();
	}
}