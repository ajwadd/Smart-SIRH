package com.smarthr.service;

public interface MinioService {
    
    /**
     * Téléverse un fichier (tableau d'octets) sur MinIO.
     * 
     * @param objectName Le nom de l'objet ou chemin relatif dans le bucket.
     * @param content Le contenu du fichier en tableau d'octets.
     * @param contentType Le type MIME du fichier (ex: application/pdf).
     * @return Le chemin ou nom de l'objet téléversé.
     */
    String uploadFile(String objectName, byte[] content, String contentType);

    /**
     * Télécharge un fichier depuis MinIO.
     * 
     * @param objectName Le nom ou chemin de l'objet dans le bucket.
     * @return Le tableau d'octets représentant le contenu du fichier.
     */
    byte[] downloadFile(String objectName);

    /**
     * Supprime un fichier dans le stockage MinIO.
     * 
     * @param objectName Le nom de l'objet à supprimer.
     */
    void deleteFile(String objectName);
}
