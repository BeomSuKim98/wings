// src/main/java/com/example/post/controller/PostController.java
package com.wings.aei.controller;

import com.wings.aei.dto.PostCreateDto;
import com.wings.aei.service.PostService;
import com.wings.aei.utility.HtmlSanitizerUtil; // 정제 유틸(패키지 맞게 변경)
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/new")
    public String newForm(HttpServletRequest req, Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PostCreateDto());
        }
        // 탭/프래그먼트 사용 안 하면 그냥 "post/form"만 반환하면 됩니다.
        boolean ajax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
        return ajax ? "post/form :: content" : "post/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") PostCreateDto form,
                         BindingResult binding,
                         HttpServletRequest req,
                         Model model) {
        if (binding.hasErrors()) {
            // 검증 실패 시 다시 폼으로
            boolean ajax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
            return ajax ? "post/form :: content" : "post/form";
        }

        // ✅ 정제는 Service 내부에서 수행
        Long postId = postService.createPost(form);
        return "redirect:/posts/" + postId;
    }
}
