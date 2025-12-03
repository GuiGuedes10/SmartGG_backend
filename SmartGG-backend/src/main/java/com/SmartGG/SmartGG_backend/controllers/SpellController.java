package com.SmartGG.SmartGG_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SmartGG.SmartGG_backend.services.SpellService;

    @RestController
    @RequestMapping("/spells")
    public class SpellController {

        @Autowired
        private SpellService spellService;

        @GetMapping("/name/{id}")
        public String getSpellName(@PathVariable int id) {
            return spellService.getSpellName(id);
        }
    }


