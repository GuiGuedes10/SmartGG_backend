package com.SmartGG.SmartGG_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SmartGG.SmartGG_backend.services.ChampionService;

@RestController
@RequestMapping("/champion")
public class ChampionController {

    @Autowired
    private ChampionService championService;

    @GetMapping("/{id}")
    public String getChampionName(@PathVariable String id) {
        return championService.getChampionName(id);
    }
}

