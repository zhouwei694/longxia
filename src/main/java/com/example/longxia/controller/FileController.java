package com.example.longxia.controller;

import com.example.longxia.common.BaseResponse;
import com.example.longxia.common.ResultUtils;
import com.example.longxia.exception.BusinessException;
import com.example.longxia.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传 控制层
 *
 * @author zhouwei
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持 jpg/png/gif/webp/bmp 格式的图片");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toAbsolutePath().toFile());

            // 返回访问URL
            String fileUrl = "/file/download/" + newFilename;
            return ResultUtils.success(fileUrl);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }
    }

    /**
     * 下载/预览图片
     */
    @GetMapping("/download/{filename}")
    public void downloadImage(@PathVariable String filename, HttpServletResponse response) {
        // 防止路径穿越
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法文件名");
        }

        Path filePath = Paths.get(uploadDir).toAbsolutePath().resolve(filename);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }

        try {
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);
            response.setContentLengthLong(Files.size(filePath));
            // 设置缓存，图片资源可缓存7天
            response.setHeader("Cache-Control", "max-age=604800");

            try (OutputStream os = response.getOutputStream()) {
                Files.copy(filePath, os);
                os.flush();
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件读取失败");
        }
    }
}
