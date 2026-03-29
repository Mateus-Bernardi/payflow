package com.payflow.ms_wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MsWalletApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsWalletApplication.class, args);
	}

}
