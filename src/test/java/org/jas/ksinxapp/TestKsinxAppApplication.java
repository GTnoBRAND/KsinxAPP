package org.jas.ksinxapp;

import org.springframework.boot.SpringApplication;

public class TestKsinxAppApplication {

    public static void main(String[] args) {
        SpringApplication.from(KsinxAppApplication::main).with(IntegrationTestContainers.class).run(args);
    }

}
