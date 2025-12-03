package com.SmartGG.SmartGG_backend.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SmartGG.SmartGG_backend.services.RuneService;

@RestController
@RequestMapping("/runes")
public class RuneController {

    @Autowired
    private RuneService runeService;

    @GetMapping("/{id}")
    public Map<String, String> getRuneById(@PathVariable String id) {
        Map<String, String> rune = runeService.getRuneById(id);
        if (rune == null) {
            throw new RuntimeException("Rune not found for id: " + id);
        }
        return rune;
    }
}
