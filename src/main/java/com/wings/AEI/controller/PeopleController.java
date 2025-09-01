package com.wings.AEI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PeopleController {
    @GetMapping("/people/new/fragment")
    public String newPe() {
        return "people/new :: content";
    }
}
