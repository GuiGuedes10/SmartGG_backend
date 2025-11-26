package com.SmartGG.SmartGG_backend.services;

import org.springframework.stereotype.Service;

@Service
public class testeService {
    public String teste(String name){
        return "Hello World " + name + "!";
    }
}
