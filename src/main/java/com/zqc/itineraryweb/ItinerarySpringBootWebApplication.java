package com.zqc.itineraryweb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@EnableAspectJAutoProxy
@SpringBootApplication
public class ItinerarySpringBootWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItinerarySpringBootWebApplication.class, args);
        log.info("ItinerarySpringBootWebApplication 项目启动成功！");
    }

}
