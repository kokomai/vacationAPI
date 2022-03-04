package com.foresys.vacationAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
public class VacationApiApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(VacationApiApplication.class);
		application.addListeners(new ApplicationPidFileWriter());
		application.run(args);
	}

}
