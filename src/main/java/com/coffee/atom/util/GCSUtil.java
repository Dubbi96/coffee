package com.coffee.atom.util;

import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.file.FileEventLogType;
import com.coffee.atom.service.file.FileEventLogService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.*;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class GCSUtil {
    @Value("${gcs.bucket.name}")
    private String bucketName;

    private final FileEventLogService fileEventLogService;
    private final Storage storage;

    public GCSUtil(@Value("${gcs.credentials.path}") String credentialsPath,
                   @Value("${gcs.project.id}") String projectId,
                   FileEventLogService fileEventLogService) throws IOException {
        this.fileEventLogService = fileEventLogService;
        InputStream keyFile;
        if (credentialsPath.startsWith("classpath:")) {
            keyFile = new ClassPathResource(credentialsPath.replace("classpath:", "")).getInputStream();
        } else if (credentialsPath.startsWith("/") || credentialsPath.startsWith("file:")) {
            keyFile = new FileInputStream(credentialsPath);
        } else {
            keyFile = new ClassPathResource(credentialsPath).getInputStream();
        }

        storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(GoogleCredentials.fromStream(keyFile))
                .build().getService();
    }

    @PreDestroy
    public void shutdown() throws Exception {
        log.info("Shutting down GCS Storage client...");
        storage.close();
    }

    public String uploadFileToGCS(String directory, MultipartFile file, AppUser appUser) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String savedFileName = generateUniqueFileName(originalFilename);
        String filePath = (directory != null ? directory + "/" : "") + savedFileName;

        try {
            storage.create(
                    BlobInfo.newBuilder(bucketName, filePath)
                            .setContentType(file.getContentType())
                            .setContentDisposition("inline")
                            .build(),
                    file.getBytes()
            );

            String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + filePath;
            fileEventLogService.saveLog(appUser, FileEventLogType.UPLOAD, file, fileUrl, true);
            return fileUrl;
        } catch (IOException e) {
            fileEventLogService.saveLog(appUser, FileEventLogType.UPLOAD, file, filePath, false);
            throw e;
        }
    }

    public List<String> uploadFilesToGCS(String directory, List<MultipartFile> files, AppUser appUser) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFileToGCS(directory, file, appUser));
        }
        fileEventLogService.saveLogs(appUser, FileEventLogType.UPLOAD, files, urls, true);
        return urls;
    }

    public InputStream downloadFileFromGCS(String fileUrl, AppUser appUser) {
        String objectName = extractObjectName(fileUrl);
        Blob blob = storage.get(bucketName, objectName);

        if (blob == null) {
            fileEventLogService.saveDownloadLog(appUser, fileUrl, false);
            throw new IllegalArgumentException("파일을 찾을 수 없습니다: " + fileUrl);
        }

        fileEventLogService.saveDownloadLog(appUser, fileUrl, true);
        ReadChannel reader = blob.reader();
        return Channels.newInputStream(reader);
    }

    public void deleteFileFromGCS(String fileUrl, AppUser appUser) {
        String objectName = extractObjectName(fileUrl);
        BlobId blobId = BlobId.of(bucketName, objectName);

        boolean deleted = storage.delete(blobId);
        fileEventLogService.saveDeleteLogs(List.of(fileUrl), appUser);

        if (!deleted) {
            throw new IllegalArgumentException("파일 삭제 실패: " + fileUrl);
        }
    }

    public void deleteFileFromGCS(List<String> fileUrls, AppUser appUser) {
        for (String url : fileUrls) {
            deleteFileFromGCS(url, appUser);
        }
        fileEventLogService.saveDeleteLogs(fileUrls, appUser);
    }

    public MediaType getMediaType(String fileUrl) {
        String lowerUrl = fileUrl.toLowerCase();

        if (lowerUrl.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (lowerUrl.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private static final String UUID_SEPARATOR = "__uuid__";

    private String generateUniqueFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        String extension = "";
        String baseName = originalFileName;

        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            extension = originalFileName.substring(lastDotIndex);
            baseName = originalFileName.substring(0, lastDotIndex);
        }

        String uuid = UUID.randomUUID().toString();
        return baseName + UUID_SEPARATOR + uuid + extension;
    }

    private String extractOriginalFileName(String savedFileName) {
        if (savedFileName == null || !savedFileName.contains(UUID_SEPARATOR)) {
            return savedFileName;
        }

        int separatorIndex = savedFileName.indexOf(UUID_SEPARATOR);
        String baseName = savedFileName.substring(0, separatorIndex);

        String extension = "";
        int lastDotIndex = savedFileName.lastIndexOf('.');
        if (lastDotIndex > separatorIndex) {
            extension = savedFileName.substring(lastDotIndex);
        }

        return baseName + extension;
    }

    private String extractObjectName(String fileUrl) {
        String prefix = "https://storage.googleapis.com/" + bucketName + "/";
        if (!fileUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("올바르지 않은 GCS URL: " + fileUrl);
        }
        return fileUrl.substring(prefix.length());
    }
}
