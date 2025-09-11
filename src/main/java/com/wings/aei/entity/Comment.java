package com.wings.aei.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"post", "author", "parent", "children"})
@Entity
@Table(name = "comment",
        indexes = {
                @Index(name = "idx_comment_post_depth_created", columnList = "post_id, depth, created_at"),
                @Index(name = "idx_comment_parent_created", columnList = "parent_id, created_at")
        })
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 자기참조(대댓글)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;              // 루트 댓글이면 null

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<Comment> children = new ArrayList<>();

    @Column(nullable = false)
    private short depth;                 // 0=루트, 1=대댓글, ...

    @Lob
    @Column(name = "content_html", nullable = false, columnDefinition = "LONGTEXT")
    private String contentHtml;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;                 // 완전 익명 허용 시 null 가능

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Column(name = "anon_display_name", length = 50)
    private String anonDisplayName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.depth < 0) this.depth = 0;
        if (this.anonDisplayName == null && this.anonymous) this.anonDisplayName = "익명";
    }

    // 편의 메서드
    public void setParentAndDepth(Comment parent) {
        this.parent = parent;
        this.depth = (short) ((parent == null) ? 0 : parent.getDepth() + 1);
        if (parent != null) parent.getChildren().add(this);
    }
}
