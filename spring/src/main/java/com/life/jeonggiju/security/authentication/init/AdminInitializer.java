package com.life.jeonggiju.security.authentication.init;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Profile({"local", "dev"})
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

	private final InitService initService;

	@Override
	public void run(ApplicationArguments args) {
		initService.initAdmin();
		initService.initDefaultUser();
	}
}
