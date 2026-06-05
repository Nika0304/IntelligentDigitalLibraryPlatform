package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DigitalLibraryApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(DigitalLibraryApplication.class, args);
    }
}