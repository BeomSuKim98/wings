package com.wings.aei.utility;

import org.owasp.html.*;

import com.wings.aei.service.ImageService;
import java.net.*;
import java.util.*;
import java.util.regex.*;


private static final AttributePolicy OUR_IMG_SRC = (elem, attr, value) -> {
    try {
        URI u = new URI(value);
        String scheme = (u.getScheme() == null) ? "" : u.getScheme().toLowerCase();
        String host   = (u.getHost() == null)   ? "" : u.getHost().toLowerCase();
        String path   = (u.getPath() == null)   ? "" : u.getPath();

        boolean okScheme = "http".equals(scheme) || "https".equals(scheme);
        boolean okHost   = host.equals("cdn.example.com"); // ← 우리 CDN/도메인
        boolean okPath   = path.startsWith("/uploads/");
        return (okScheme && okHost && okPath) ? value : null;
    } catch (Exception e) { return null; }
};

// 게시글용 정책에서 img 속성에 OUR_IMG_SRC 적용
.allowElements("img")
.allowAttributes("src").matching(OUR_IMG_SRC).onElements("img")