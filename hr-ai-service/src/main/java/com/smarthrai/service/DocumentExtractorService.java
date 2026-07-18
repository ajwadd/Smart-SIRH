package com.smarthrai.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@Slf4j
public class DocumentExtractorService {

    private final Tika tika = new Tika();

    public String extractText(byte[] content, String fileName) {
        log.info("Extraction de texte du fichier : {}", fileName);
        try (InputStream stream = new ByteArrayInputStream(content)) {
            String extracted = tika.parseToString(stream);
            log.info("Texte extrait avec succès ({} caractères).", extracted.length());
            return extracted;
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction de texte de '{}' : {}", fileName, e.getMessage());
            throw new RuntimeException("Erreur d'extraction de texte : " + e.getMessage(), e);
        }
    }
}
