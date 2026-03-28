package com.example.longxia;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.example.longxia.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class LongxiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(LongxiaApplication.class, args);
    }

}


