package com.irtech.brokerinfrastructure;

import com.irtech.brokerinfrastructure.context.SepehrProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BrokerinfrastructureApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrokerinfrastructureApplication.class, args);
	}

}
