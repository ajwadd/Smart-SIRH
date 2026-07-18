package com.smarthr.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfig {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        try {
            log.info("Initialisation du client MinIO à l'adresse : {}", minioUrl);
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .build();

            try {
                boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                if (!found) {
                    log.info("Le bucket '{}' n'existe pas. Création en cours...", bucketName);
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    log.info("Le bucket MinIO '{}' a été créé avec succès.", bucketName);
                } else {
                    log.info("Le bucket MinIO '{}' existe déjà.", bucketName);
                }
            } catch (Exception e) {
                log.warn("⚠️ Impossible de se connecter à MinIO lors de l'initialisation du bucket. Assurez-vous que MinIO est démarré sur '{}'. Détail : {}", minioUrl, e.getMessage());
            }

            return minioClient;
        } catch (Exception e) {
            log.error("Erreur critique lors de la construction du client MinIO : ", e);
            throw new RuntimeException("Erreur de configuration du client MinIO: " + e.getMessage(), e);
        }
    }

    @Bean
    public String minioBucketName() {
        return this.bucketName;
    }
}
