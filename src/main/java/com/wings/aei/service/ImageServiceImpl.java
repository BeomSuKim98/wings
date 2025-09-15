package com.wings.aei.service;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class ImageServiceImpl implements ImageService {

    // 허용 MIME: JPEG/PNG
    private static final Set<String> ALLOWED_MIME = Set.of(
            "image/jpeg", "image/png"
    );

    private static final long MAX_BYTES = 5L * 1024 * 1024;   // 5MB
    private static final int  MAX_W = 8000, MAX_H = 8000;
    private static final long MAX_PIXELS = 20_000_000;        // 20MP

    private final Path root = Path.of("/var/www/uploads"); // TODO: 환경값 주입 권장
    private final String publicBaseUrl = "https://cdn.example.com/uploads"; // TODO 교체

    @Override
    public String processAndStore(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequest("empty file");
        if (file.getSize() > MAX_BYTES)     throw new BadRequest("file too large");

        byte[] bytes = readAll(file);
        String mime = sniffMime(bytes);
        if (!ALLOWED_MIME.contains(mime)) {
            throw new BadRequest("unsupported mime: " + mime + " (only JPEG, PNG allowed)");
        }

        BufferedImage src = readImage(bytes);
        int w = src.getWidth(), h = src.getHeight();
        if (w <= 0 || h <= 0 || w > MAX_W || h > MAX_H || (long)w * h > MAX_PIXELS) {
            throw new BadRequest("too large resolution");
        }

        BufferedImage safe = downscaleIfNeeded(src, 2560);

        String ext = ("image/png".equals(mime)) ? "png" : "jpg";
        String filename = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(filename).normalize();

        writeEncoded(safe, ext, target);
        return publicBaseUrl + "/" + filename;
    }

    private byte[] readAll(MultipartFile f) {
        try { return f.getBytes(); } catch (Exception e) { throw new BadRequest("read fail"); }
    }

    // 간단한 매직넘버 검사 (JPEG/PNG만)
    private String sniffMime(byte[] b) {
        if (b.length >= 3 && (b[0]&0xFF)==0xFF && (b[1]&0xFF)==0xD8) return "image/jpeg";
        if (b.length >= 8 && (b[0]&0xFF)==0x89 && b[1]=='P' && b[2]=='N' && b[3]=='G') return "image/png";
        throw new BadRequest("unsupported or unknown image format");
    }

    private BufferedImage readImage(byte[] b) {
        try (var in = new ByteArrayInputStream(b)) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) throw new BadRequest("decode fail");
            return img;
        } catch (Exception e) {
            throw new BadRequest("decode fail");
        }
    }

    private BufferedImage downscaleIfNeeded(BufferedImage src, int maxEdge) {
        int w = src.getWidth(), h = src.getHeight();
        int max = Math.max(w, h);
        if (max <= maxEdge) return src;
        double scale = (double) maxEdge / max;
        int nw = (int)Math.round(w * scale);
        int nh = (int)Math.round(h * scale);
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        return out;
    }

    private void writeEncoded(BufferedImage img, String ext, Path target) {
        try {
            Files.createDirectories(target.getParent());
            try (var out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
                boolean ok = ImageIO.write(img, ext, out);
                if (!ok) throw new IOException("no writer for " + ext);
            }
            // 권한 설정이 필요한 환경이면 추가
        } catch (Exception e) {
            throw new BadRequest("store fail");
        }
    }

    public static class BadRequest extends RuntimeException {
        public BadRequest(String m) { super(m); }
    }
}