package com.wings.AEI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping
public class PageController {

    @GetMapping("/home")
    public String home(Model model){
        model.addAttribute("title","홈");
        model.addAttribute("body","/WEB-INF/views/home/index.jsp"); // 본문 JSP 논리이름
        return "common/layout";
    }

}
