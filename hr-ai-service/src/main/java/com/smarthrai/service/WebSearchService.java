package com.smarthrai.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSearchService {

    @Value("${searxng.url:http://localhost:8888}")
    private String searxngUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Domaines NSFW ou non pertinents à bloquer
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
            "reddit.com", "chaturbate.com", "pornhub.com", "xvideos.com",
            "xnxx.com", "xhamster.com", "onlyfans.com", "jedonne.fr",
            "donnons.org", "leboncoin.fr", "stackoverflow.com",
            "github.com", "support.google.com", "facebook.com",
            "twitter.com", "x.com", "instagram.com", "tiktok.com"
    );

    public String searchWeb(String query) {
        if (searxngUrl == null || searxngUrl.trim().isEmpty()) {
            log.warn("URL de SearXNG non configurée. Recherche Web désactivée.");
            return "Recherche Web indisponible.";
        }

        log.info("Exécution d'une recherche Web via SearXNG pour la requête : '{}'", query);

        try {
            String targetUrl = UriComponentsBuilder.fromHttpUrl(searxngUrl)
                    .path("/search")
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("engines", "google,bing,duckduckgo")
                    .queryParam("language", "fr-FR")
                    .queryParam("safesearch", "2") // SafeSearch strict
                    .queryParam("categories", "general")
                    .build()
                    .toUriString();

            SearxngResponse response = restTemplate.getForObject(targetUrl, SearxngResponse.class);

            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                // Filtrer les résultats non pertinents et NSFW
                List<SearxngResult> filteredResults = response.getResults().stream()
                        .filter(r -> r.getUrl() != null && !isBlockedDomain(r.getUrl()))
                        .filter(r -> r.getContent() != null && !r.getContent().trim().isEmpty())
                        .filter(r -> isRelevant(r, query))
                        .limit(5)
                        .toList();

                if (filteredResults.isEmpty()) {
                    log.warn("Tous les résultats SearXNG ont été filtrés comme non pertinents.");
                    return "Aucun résultat pertinent trouvé sur le web pour votre question.";
                }

                // Formater proprement les résultats
                return formatResults(query, filteredResults);
            }

        } catch (Exception e) {
            log.error("Échec de la recherche Web via SearXNG à l'adresse '{}' : {}", searxngUrl, e.getMessage());
        }

        return "Aucun résultat trouvé sur le Web via SearXNG (vérifiez si l'instance est démarrée).";
    }

    private boolean isBlockedDomain(String url) {
        String urlLower = url.toLowerCase();
        for (String blocked : BLOCKED_DOMAINS) {
            if (urlLower.contains(blocked)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRelevant(SearxngResult result, String query) {
        String title = (result.getTitle() != null ? result.getTitle() : "").toLowerCase();
        String content = (result.getContent() != null ? result.getContent() : "").toLowerCase();
        String combined = title + " " + content;

        // Extraire les mots significatifs de la requête (> 3 chars)
        String[] queryWords = query.toLowerCase().split("[\\s',.?!]+");
        int matchCount = 0;
        for (String word : queryWords) {
            if (word.length() > 3 && combined.contains(word)) {
                matchCount++;
            }
        }

        // Au moins 1 mot significatif de la requête doit apparaître dans le résultat
        return matchCount >= 1;
    }

    private String formatResults(String query, List<SearxngResult> results) {
        StringBuilder sb = new StringBuilder();

        for (SearxngResult r : results) {
            String content = r.getContent() != null ? r.getContent().trim() : "";

            // Nettoyer le contenu
            if (content.endsWith("...")) {
                content = content.substring(0, content.length() - 3).trim();
            }
            if (content.endsWith("…")) {
                content = content.substring(0, content.length() - 1).trim();
            }

            if (!content.isEmpty()) {
                sb.append(content).append("\n");
            }
        }

        return sb.toString().trim();
    }

    @Data
    public static class SearxngResponse {
        private List<SearxngResult> results;
    }

    @Data
    public static class SearxngResult {
        private String title;
        private String url;
        private String content;
    }
}
