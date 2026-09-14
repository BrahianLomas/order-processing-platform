package org.example.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@ComponentScan("org.example")
@EnableKafka
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}