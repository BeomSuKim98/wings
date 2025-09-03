package com.wings.AEI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping
public class PageController {

    @GetMapping("/home")
    public String home(Model model){
        model.addAttribute("tabId", "home");
        model.addAttribute("title", "메인화면");
        model.addAttribute("repo", "home/main");
        model.addAttribute("frag", "content");
        return "layout";
    }

    @GetMapping("/home/main")
    public String MainPage(Model model) {
        model.addAttribute("pageCss", "page/home/mainContent.css");
        return "home/main :: content";
    }

    @GetMapping("/home/test")
    public String test(Model model) {
        return "home/test :: content";
    }
}

