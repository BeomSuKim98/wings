package com.wings.AEI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AbnormalityController {
    @GetMapping("/abnormalities/new/fragment")
    public String newAb() {
        return "abnormality/new :: content"; // 본문 프래그먼트만
    }
}
