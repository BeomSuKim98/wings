package com.wings.aei.repository;


import com.wings.aei.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> { }