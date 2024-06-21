package com.zqc.itineraryweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.zqc.itineraryweb.dao")
@EnableAspectJAutoProxy
@SpringBootApplication
public class ItinerarySpringBootWebApplication {

    private static final Logger logger = LoggerFactory.getLogger(ItinerarySpringBootWebApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ItinerarySpringBootWebApplication.class, args);
        logger.info("ItinerarySpringBoot 应用启动成功！");
    }

}
