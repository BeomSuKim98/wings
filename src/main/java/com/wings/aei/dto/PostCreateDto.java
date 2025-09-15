// src/main/java/com/example/post/dto/PostCreateDto.java
package com.wings.aei.dto;

import lombok.*;                          // @Getter, @Setter, @Builder, @ToString, @NoArgsConstructor, @AllArgsConstructor
import jakarta.validation.constraints.*;  // @NotBlank, @Size

/**
 * 글 생성 요청 DTO
 * - 클라이언트가 보낸 "제목/본문/익명 여부" 등을 담아 컨트롤러에서 받는 용도
 * - 엔티티 변환/정제(sanitize)는 보통 Service 층에서 처리 (DTO는 '요청 스펙'만 표현)
 */
@Getter
@Setter
@ToString(exclude = {"contentHtml"}) // 로그에 본문 통째로 안 찍히게(보안/성능상 권장)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateDto {

    /**
     * 글 제목
     * - 공백만 입력 금지
     * - 길이 제한: 1~150자
     */
    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max = 150, message = "제목은 150자 이하로 입력해 주세요.")
    private String title;

    /**
     * 글 본문(HTML)
     * - 에디터에서 넘어오는 HTML을 그대로 받되, 저장 전 Service에서 "서버측 Sanitizer"로 정제
     * - 길이 제한 예시: 최대 100,000자
     */
    @NotBlank(message = "본문을 입력해 주세요.")
    @Size(max = 100_000, message = "본문이 너무 깁니다(최대 100,000자).")
    private String contentHtml;

    /**
     * 익명 글 여부 (선택)
     * - 익명 글이면 author를 null 로 저장하거나, 별도 표시 이름 사용 정책
     */
    private boolean anonymous;

    /**
     * 익명 표시 이름(선택)
     * - anonymous=true일 때만 의미가 있음
     * - 길이 제한: 1~50자 (비어 있으면 서버에서 기본값 부여 가능)
     */
    @Size(max = 50, message = "익명 표시 이름은 50자 이하로 입력해 주세요.")
    private String anonDisplayName;
}
