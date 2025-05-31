package com.urlshortener.naataurl.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GoogleSafeBrowsingService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleSafeBrowsingService.class);

    @Value("${google.safebrowsing.api.key}")
    private String apiKey;

    private static final String ENDPOINT = "https://safebrowsing.googleapis.com/v4/threatMatches:find";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleSafeBrowsingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public boolean isUrlSafe(String url) {
        try {
            logger.info("Checking URL safety for: {}", url);

            SafeBrowsingRequest request = new SafeBrowsingRequest();
            request.setThreatInfo(new ThreatInfo(
                    List.of("THREAT_TYPE_UNSPECIFIED", "MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE"),
                    List.of("PLATFORM_TYPE_UNSPECIFIED", "ANY_PLATFORM"),
                    List.of("THREAT_ENTRY_TYPE_UNSPECIFIED", "URL"),
                    List.of(new ThreatEntry(url))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SafeBrowsingRequest> entity = new HttpEntity<>(request, headers);
            String requestUrl = ENDPOINT + "?key=" + apiKey;

            logger.debug("Making request to Google Safe Browsing API with request: {}",
                    objectMapper.writeValueAsString(request));

            ResponseEntity<SafeBrowsingResponse> response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    entity,
                    SafeBrowsingResponse.class
            );

            SafeBrowsingResponse responseBody = response.getBody();
            boolean isSafe = responseBody == null || responseBody.getMatches() == null || responseBody.getMatches().isEmpty();

            if (!isSafe) {
                logger.warn("URL {} flagged as unsafe. Threat matches: {}",
                        url, objectMapper.writeValueAsString(responseBody != null ? responseBody.getMatches() : null));
            } else {
                logger.info("URL {} passed safety check", url);
            }

            return isSafe;
        } catch (Exception e) {
            logger.error("Error checking URL safety for {}: {}", url, e.getMessage(), e);
            return false;
        }
    }

    @Data
    static class SafeBrowsingRequest {
        @JsonProperty("client")
        private final ClientInfo client = new ClientInfo("naataurl", "1.0");

        @JsonProperty("threatInfo")
        private ThreatInfo threatInfo;
    }

    @Data
    static class ClientInfo {
        private final String clientId;
        private final String clientVersion;
    }

    @Data
    static class ThreatInfo {
        @JsonProperty("threatTypes")
        private List<String> threatTypes;

        @JsonProperty("platformTypes")
        private List<String> platformTypes;

        @JsonProperty("threatEntryTypes")
        private List<String> threatEntryTypes;

        @JsonProperty("threatEntries")
        private List<ThreatEntry> threatEntries;

        public ThreatInfo(List<String> threatTypes, List<String> platformTypes,
                          List<String> threatEntryTypes, List<ThreatEntry> threatEntries) {
            this.threatTypes = threatTypes;
            this.platformTypes = platformTypes;
            this.threatEntryTypes = threatEntryTypes;
            this.threatEntries = threatEntries;
        }
    }

    @Data
    static class ThreatEntry {
        @JsonProperty("url")
        private String url;

        public ThreatEntry(String url) {
            this.url = url;
        }
    }

    @Data
    static class SafeBrowsingResponse {
        @JsonProperty("matches")
        private List<ThreatMatch> matches;
    }

    @Data
    static class ThreatMatch {
        @JsonProperty("threatType")
        private String threatType;

        @JsonProperty("platformType")
        private String platformType;

        @JsonProperty("threat")
        private ThreatEntry threat;
    }
}