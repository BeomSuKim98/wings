package com.wings.aei.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;

import java.util.*;

// ImageService.java
@Service
public class ImageService {

    private static final Set<String> ALLOWED_MIME = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_BYTES = 5L * 1024 * 1024;   // 5MB
    private static final int MAX_W = 8000, MAX_H = 8000;
    private static final long MAX_PIXELS = 20_000_000;        // 20MP

    private final Path root = Path.of("/var/www/uploads");    // 저장 루트(예시)
    private final String publicBaseUrl = "https://cdn.example.com/uploads"; // 공개 URL prefix

    public String processAndStore(MultipartFile file) {
        if (file.isEmpty()) throw new BadRequest("empty file");
        if (file.getSize() > MAX_BYTES) throw new BadRequest("file too large");

        byte[] bytes = readAll(file);
        String mime = sniffMime(bytes);             // 매직넘버로 판별
        if (!ALLOWED_MIME.contains(mime)) throw new BadRequest("unsupported mime: " + mime);

        BufferedImage src = readImage(bytes);       // 안전 디코딩(실패 시 예외)
        int w = src.getWidth(), h = src.getHeight();
        if (w <= 0 || h <= 0 || w > MAX_W || h > MAX_H || (long)w * h > MAX_PIXELS)
            throw new BadRequest("too large resolution");

        // 필요하면 리사이즈 (예: 2560px 이상이면 축소)
        BufferedImage safe = downscaleIfNeeded(src, 2560);

        // 메타데이터 제거를 위해 **새 파일로 재인코딩**
        String ext = chooseExt(mime); // "jpg" | "png" | "webp" 등
        String filename = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(filename).normalize();

        writeEncoded(safe, ext, target);

        // 퍼블릭 URL 반환 (서버는 /uploads 를 read-only 서빙)
        return publicBaseUrl + "/" + filename;
    }

    private byte[] readAll(MultipartFile f) {
        try { return f.getBytes(); } catch (Exception e) { throw new BadRequest("read fail"); }
    }

    // 매우 단순한 매직넘버 스니핑 (실무에선 더 정교하게/라이브러리 병행 권장)
    private String sniffMime(byte[] b) {
        if (b.length >= 3 && b[0]==(byte)0xFF && b[1]==(byte)0xD8) return "image/jpeg";
        if (b.length >= 8 && b[0]==(byte)0x89 && b[1]==0x50 && b[2]==0x4E && b[3]==0x47) return "image/png";
        if (b.length >= 12 && b[0]==0x52 && b[1]==0x49 && b[2]==0x46 && b[3]==0x46 && b[8]==0x57 && b[9]==0x45 && b[10]==0x42 && b[11]==0x50) return "image/webp";
        if (b.length >= 6 && b[0]=='G' && b[1]=='I' && b[2]=='F') return "image/gif";
        throw new BadRequest("unknown file signature");
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

    private String chooseExt(String mime) {
        return switch (mime) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            case "image/gif"  -> "gif"; // 애니메이션은 재인코딩 시 정지될 수 있음(주의)
            default -> "jpg";
        };
    }

    private void writeEncoded(BufferedImage img, String ext, Path target) {
        try {
            Files.createDirectories(target.getParent());
            try (var out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
                boolean ok = ImageIO.write(img, ext, out);
                if (!ok) throw new IOException("no writer for " + ext);
            }
            // 권한 최소화(리눅스): rw-r--r--
            Files.setPosixFilePermissions(target, PosixFilePermissions.fromString("rw-r--r--"));
        } catch (Exception e) {
            throw new BadRequest("store fail");
        }
    }

    static class BadRequest extends RuntimeException {
        BadRequest(String m) { super(m); }
    }
}
