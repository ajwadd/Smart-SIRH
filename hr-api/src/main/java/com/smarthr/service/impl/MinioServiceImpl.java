package com.smarthr.service.impl;

import com.smarthr.service.MinioService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final String minioBucketName;

    @Override
    public String uploadFile(String objectName, byte[] content, String contentType) {
        try {
            log.info("Téléversement du fichier '{}' dans le bucket '{}'", objectName, minioBucketName);
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object(objectName)
                            .stream(bais, content.length, -1)
                            .contentType(contentType)
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            log.error("Erreur lors de l'upload du fichier '{}' sur MinIO: ", objectName, e);
            throw new RuntimeException("Erreur d'écriture MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadFile(String objectName) {
        try {
            log.info("Téléchargement de l'objet '{}' depuis le bucket '{}'", objectName, minioBucketName);
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object(objectName)
                            .build()
            )) {
                return stream.readAllBytes();
            }
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement de l'objet '{}' sur MinIO: ", objectName, e);
            throw new RuntimeException("Erreur de lecture MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            log.info("Suppression de l'objet '{}' dans le bucket '{}'", objectName, minioBucketName);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'objet '{}' sur MinIO: ", objectName, e);
            throw new RuntimeException("Erreur de suppression MinIO: " + e.getMessage(), e);
        }
    }
}
