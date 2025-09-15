// src/main/java/com/wings/aei/utility/HtmlSanitizerUtil.java
package com.wings.aei.utility;

import org.owasp.html.*;     // HtmlPolicyBuilder, PolicyFactory, AttributePolicy
import java.net.*;           // URI
import java.util.*;          // Locale, Optional
import java.util.regex.*;    // Pattern

public final class HtmlSanitizerUtil {

    private HtmlSanitizerUtil() {}

    /** 우리 CDN 이미지만 허용: https://cdn.example.com/uploads/... 로 교체하세요 */
    private static final AttributePolicy OUR_IMG_SRC = (elem, attr, value) -> {
        try {
            URI u = new URI(value);
            String scheme = Optional.ofNullable(u.getScheme()).orElse("").toLowerCase(Locale.ROOT);
            String host   = Optional.ofNullable(u.getHost()).orElse("").toLowerCase(Locale.ROOT);
            String path   = Optional.ofNullable(u.getPath()).orElse("");
            boolean okScheme = "http".equals(scheme) || "https".equals(scheme);
            boolean okHost   = host.equals("cdn.example.com");   // TODO: 실제 CDN 도메인
            boolean okPath   = path.startsWith("/uploads/");
            return (okScheme && okHost && okPath) ? value : null;
        } catch (Exception e) { return null; }
    };

    /** YouTube 임베드 전용: https://(www.)youtube(.com|-nocookie.com)/embed/... */
    private static final AttributePolicy YT_EMBED_SRC = (elem, attr, value) -> {
        try {
            URI u = new URI(value);
            String scheme = Optional.ofNullable(u.getScheme()).orElse("").toLowerCase(Locale.ROOT);
            String host   = Optional.ofNullable(u.getHost()).orElse("").toLowerCase(Locale.ROOT);
            String path   = Optional.ofNullable(u.getPath()).orElse("");
            boolean okScheme = "http".equals(scheme) || "https".equals(scheme);
            boolean okHost   = host.endsWith("youtube.com") || host.endsWith("youtube-nocookie.com");
            boolean okPath   = path.startsWith("/embed/");
            return (okScheme && okHost && okPath) ? value : null;
        } catch (Exception e) { return null; }
    };

    /** style 값에서 허용할 CSS 선언만 통과 (color / background-color / font-size) */
    private static final AttributePolicy STYLE_WHITELIST = (elem, attr, value) -> {
        if (value == null) return null;
        // 선언 분해: "prop: val; prop2: val2"
        String[] decls = value.split(";");
        StringBuilder out = new StringBuilder();
        for (String d : decls) {
            String t = d.trim().toLowerCase(Locale.ROOT);
            if (t.startsWith("color:")
                    || t.startsWith("background-color:")
                    || t.startsWith("font-size:")) {
                if (out.length() > 0) out.append("; ");
                out.append(d.trim());
            }
        }
        return out.length() == 0 ? null : out.toString();
    };

    private static final Pattern NUMERIC_1_TO_4 = Pattern.compile("\\d{1,4}");

    /** 공통 텍스트 정책 */
    private static final PolicyFactory BASE_TEXT_POLICY =
            new HtmlPolicyBuilder()
                    .allowElements("p","span","strong","b","em","i","u","s",
                            "ul","ol","li","h1","h2","h3",
                            "blockquote","code","pre","br","hr","a")
                    .allowAttributes("href").onElements("a")
                    .allowUrlProtocols("http","https","mailto")
                    .requireRelNofollowOnLinks()
                    .requireRelsOnLinks("noopener","noreferrer")   // ← requireRelsOnLinks 아님
                    .allowAttributes("title").onElements("a")
                    // style 제한 허용
                    .allowAttributes("style").matching(STYLE_WHITELIST)
                    .onElements("p","span","a","h1","h2","h3","li","code","pre")
                    .toFactory();

    /** 게시글: 이미지(우리 CDN) + YouTube iframe 허용 */
    private static final PolicyFactory POST_MEDIA_POLICY =
            new HtmlPolicyBuilder()
                    // img (우리 CDN만)
                    .allowElements("img")
                    .allowAttributes("src").matching(OUR_IMG_SRC).onElements("img")
                    .allowAttributes("alt","title").onElements("img")
                    .allowAttributes("width").matching(NUMERIC_1_TO_4).onElements("img")
                    .allowAttributes("height").matching(NUMERIC_1_TO_4).onElements("img")
                    // iframe (YouTube embed만)
                    .allowElements("iframe")
                    .allowAttributes("src").matching(YT_EMBED_SRC).onElements("iframe")
                    .allowAttributes("width").matching(NUMERIC_1_TO_4).onElements("iframe")
                    .allowAttributes("height").matching(NUMERIC_1_TO_4).onElements("iframe")
                    .allowAttributes("frameborder").matching(NUMERIC_1_TO_4).onElements("iframe")
                    .allowAttributes("allow").onElements("iframe")
                    .allowAttributes("allowfullscreen").onElements("iframe")
                    .allowAttributes("referrerpolicy").onElements("iframe")
                    .toFactory()
                    .and(BASE_TEXT_POLICY);

    /** 댓글: 텍스트만 */
    private static final PolicyFactory COMMENT_TEXT_ONLY_POLICY = BASE_TEXT_POLICY;

    public static String sanitizePostHtml(String dirtyHtml) {
        if (dirtyHtml == null || dirtyHtml.isEmpty()) return "";
        return POST_MEDIA_POLICY.sanitize(dirtyHtml);
    }

    public static String sanitizeCommentHtml(String dirtyHtml) {
        if (dirtyHtml == null || dirtyHtml.isEmpty()) return "";
        return COMMENT_TEXT_ONLY_POLICY.sanitize(dirtyHtml);
    }
}
