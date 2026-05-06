package com.tarique.bankstatement;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Test {
	
	public static void main(String[] args) {
		 BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
	        String plainPassword = "Admin@123";
	        String hash = encoder.encode(plainPassword);
	        System.out.println("BCrypt hash for 'Admin@123': " + hash);

	        // Test if the hash matches
	        boolean matches = encoder.matches(plainPassword, hash);
	        System.out.println("Hash verification: " + matches);
	}

}
