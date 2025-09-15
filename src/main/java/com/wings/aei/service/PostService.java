package com.wings.aei.service;


import com.wings.aei.dto.PostCreateDto;
import com.wings.aei.entity.Post;
import com.wings.aei.repository.PostRepository;
import com.wings.aei.utility.HtmlSanitizerUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    public Long createPost(PostCreateDto dto) {

        String safeHtml = HtmlSanitizerUtil.sanitizePostHtml(dto.getContentHtml());


        Post post = Post.builder()
                .title(dto.getTitle())
                .contentHtml(dto.getContentHtml())   // 이미 sanitize된 내용
                .anonymous(dto.isAnonymous())
                .anonDisplayName(dto.getAnonDisplayName())
                // .author(currentUser) // 로그인 사용자로 세팅 (예: SecurityContext에서)
                .build();

        postRepository.save(post);
        return post.getId();


    }

    @Transactional(readOnly = true)
    public Post getById(Long id) {
        return postRepository.findById(id).orElseThrow();
    }
}
