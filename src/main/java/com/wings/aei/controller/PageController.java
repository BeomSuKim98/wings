package com.wings.aei.controller;

import com.wings.aei.dto.PostCreateDto;
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
        model.addAttribute("tabId", "home");
        model.addAttribute("title", "메인화면");
        model.addAttribute("repo", "home/main");
        model.addAttribute("frag", "content");
        return "home/main :: content";
    }

    @GetMapping("/home/test")
    public String test(Model model) {
        model.addAttribute("tabId", "test");
        model.addAttribute("title", "메인화면");
        model.addAttribute("repo", "home/test");
        model.addAttribute("frag", "content");
        return "home/test :: content";
    }

    @GetMapping("/common/newWrite")
    public String newWrite(Model model) {
        model.addAttribute("form", new PostCreateDto());
        model.addAttribute("tabId", "newWrite");
        model.addAttribute("title", "새 문서 작성");
        model.addAttribute("repo", "common/newWrite");
        model.addAttribute("frag", "content");
        return "common/newWrite :: content";
    }
}

