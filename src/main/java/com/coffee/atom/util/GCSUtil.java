package com.coffee.atom.util;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
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
    private final String gcsUrlPrefix;

    public GCSUtil(@Value("${gcs.credentials.path}") String credentialsPath,
                   @Value("${gcs.project.id}") String projectId,
                   FileEventLogService fileEventLogService) throws IOException {
        this.fileEventLogService = fileEventLogService;
        
        // credentials 파일을 try-with-resources로 처리하여 리소스 누수 방지
        try (InputStream keyFile = getCredentialsInputStream(credentialsPath)) {
            storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(GoogleCredentials.fromStream(keyFile))
                    .build().getService();
        }
        
        // GCS URL prefix를 미리 계산하여 재사용
        this.gcsUrlPrefix = "https://storage.googleapis.com/" + bucketName + "/";
    }

    private InputStream getCredentialsInputStream(String credentialsPath) throws IOException {
        if (credentialsPath.startsWith("classpath:")) {
            return new ClassPathResource(credentialsPath.replace("classpath:", "")).getInputStream();
        } else if (credentialsPath.startsWith("/") || credentialsPath.startsWith("file:")) {
            return new FileInputStream(credentialsPath);
        } else {
            return new ClassPathResource(credentialsPath).getInputStream();
        }
    }

    @PreDestroy
    public void shutdown() throws Exception {
        log.info("Shutting down GCS Storage client...");
        storage.close();
    }

    public String uploadFileToGCS(String directory, MultipartFile file, AppUser appUser) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String savedFileName = generateUniqueFileName(originalFilename);
        String filePath = buildFilePath(directory, savedFileName);
        String fileUrl = gcsUrlPrefix + filePath;

        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, filePath)
                    .setContentType(file.getContentType())
                    .setContentDisposition("inline")
                    .build();

            // MultipartFile은 이미 메모리에 로드되어 있으므로 getBytes() 사용
            // 큰 파일의 경우에도 MultipartFile이 이미 메모리에 있으므로 getBytes()가 효율적
            storage.create(blobInfo, file.getBytes());

            fileEventLogService.saveLog(appUser, FileEventLogType.UPLOAD, file, fileUrl, true);
            return fileUrl;
        } catch (IOException e) {
            fileEventLogService.saveLog(appUser, FileEventLogType.UPLOAD, file, filePath, false);
            throw e;
        }
    }

    private String buildFilePath(String directory, String fileName) {
        return (directory != null && !directory.isEmpty()) ? directory + "/" + fileName : fileName;
    }

    public List<String> uploadFilesToGCS(String directory, List<MultipartFile> files, AppUser appUser) throws IOException {
        // uploadFileToGCS 내부에서 이미 개별 로그를 저장하므로
        // 배치 로그만 별도로 저장 (중복 방지)
        List<String> urls = new ArrayList<>(files.size());
        
        for (MultipartFile file : files) {
            try {
                String url = uploadFileToGCS(directory, file, appUser);
                urls.add(url);
            } catch (IOException e) {
                // 실패한 파일의 경우 null 추가 (개별 로그는 uploadFileToGCS에서 이미 저장됨)
                urls.add(null);
                log.warn("파일 업로드 실패: {}", file.getOriginalFilename(), e);
            }
        }
        
        // 성공한 파일들에 대한 배치 로그 저장 (개별 로그와 별도로)
        List<MultipartFile> successFiles = new ArrayList<>();
        List<String> successUrls = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            if (urls.get(i) != null) {
                successFiles.add(files.get(i));
                successUrls.add(urls.get(i));
            }
        }
        
        if (!successFiles.isEmpty()) {
            fileEventLogService.saveLogs(appUser, FileEventLogType.UPLOAD, successFiles, successUrls, true);
        }
        
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
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        // 배치 삭제를 위해 BlobId 리스트 생성
        List<BlobId> blobIds = new ArrayList<>(fileUrls.size());
        for (String fileUrl : fileUrls) {
            try {
                String objectName = extractObjectName(fileUrl);
                blobIds.add(BlobId.of(bucketName, objectName));
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 파일 URL로 인해 삭제 건너뜀: {}", fileUrl, e);
            }
        }

        // 배치 삭제 실행
        if (!blobIds.isEmpty()) {
            List<Boolean> deleteResults = storage.delete(blobIds);
            
            // 삭제 실패한 파일이 있는지 확인
            for (int i = 0; i < deleteResults.size(); i++) {
                if (!deleteResults.get(i)) {
                    log.warn("파일 삭제 실패: {}", fileUrls.get(i));
                }
            }
        }

        // 배치 로그 저장 (개별 삭제와 달리 한 번만 저장)
        fileEventLogService.saveDeleteLogs(fileUrls, appUser);
    }

    public MediaType getMediaType(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        // 마지막 점의 위치를 찾아 확장자 추출 (더 효율적)
        int lastDotIndex = fileUrl.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileUrl.length() - 1) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        String extension = fileUrl.substring(lastDotIndex + 1).toLowerCase();
        
        return switch (extension) {
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    private static final String UUID_SEPARATOR = "__uuid__";

    private String generateUniqueFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new CustomException(ErrorValue.FILE_NAME_INVALID);
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

    private String extractObjectName(String fileUrl) {
        if (!fileUrl.startsWith(gcsUrlPrefix)) {
            throw new CustomException(ErrorValue.GCS_URL_INVALID);
        }
        return fileUrl.substring(gcsUrlPrefix.length());
    }
}
