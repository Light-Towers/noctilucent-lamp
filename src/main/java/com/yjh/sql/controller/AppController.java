package com.yjh.sql.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {
    @RequestMapping("/hello")
    String home(){
        return "hello,world!";
    }

}
