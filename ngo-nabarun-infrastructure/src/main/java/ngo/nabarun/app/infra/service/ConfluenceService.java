package ngo.nabarun.app.infra.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ConfluenceService {
    private final String confluenceBaseUrl;
    private final String apiToken;
    private final String username;

    /**
     * @param baseUrl Example: "https://your-domain.atlassian.net/wiki"
     * @param username Atlassian account email
     * @param apiToken Atlassian API token (see: https://id.atlassian.com/manage-profile/security/api-tokens)
     */
    public ConfluenceService(String baseUrl, String username, String apiToken) {
        this.confluenceBaseUrl = baseUrl;
        this.username = username;
        this.apiToken = apiToken;
    }

    private String buildAuthHeader() {
        String auth = username + ":" + apiToken;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Create a new page in the specified Confluence space
     */
    public int createPage(String spaceKey, String title, String content) throws Exception {
        String url = confluenceBaseUrl + "/rest/api/content";
        String payload = "{"
            + "\"type\": \"page\","
            + "\"title\": \"" + escapeJson(title) + "\"," 
            + "\"space\": {\"key\": \"" + escapeJson(spaceKey) + "\"},"
            + "\"body\": {"
            +    "\"storage\": {"
            +        "\"value\": \"" + escapeJson(content) + "\","
            +        "\"representation\": \"storage\""
            +    "}"
            + "}"
            + "}";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", buildAuthHeader());
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        return conn.getResponseCode();
    }

    /**
     * Update an existing page in Confluence (requires current version)
     */
    public int updatePage(long pageId, int version, String title, String content) throws Exception {
        String url = confluenceBaseUrl + "/rest/api/content/" + pageId;
        String payload = "{"
            + "\"version\": {\"number\": " + (version + 1) + "},"
            + "\"type\": \"page\","
            + "\"title\": \"" + escapeJson(title) + "\","
            + "\"body\": {"
            +    "\"storage\": {"
            +        "\"value\": \"" + escapeJson(content) + "\","
            +        "\"representation\": \"storage\""
            +    "}"
            + "}"
            + "}";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", buildAuthHeader());
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        return conn.getResponseCode();
    }

    private String escapeJson(String src) {
        if (src == null) return "";
        return src.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t")
                 .replace("\b", "\\b")
                 .replace("\f", "\\f");
    }

    /**
     * Retrieve the HTML content of a Confluence page by its ID.
     * @param pageId The Confluence page ID.
     * @return HTML content as a String, or null if the request fails.
     */
    public String getPageContentHtml(long pageId) throws Exception {
        String url = confluenceBaseUrl + "/rest/api/content/" + pageId + "?expand=body.storage";
        }
    
        try (java.io.InputStream is = conn.getInputStream();
             java.io.InputStreamReader reader = new java.io.InputStreamReader(is);
             java.io.BufferedReader br = new java.io.BufferedReader(reader)) {
        
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        
            // Use regex with proper JSON escaping awareness
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"value\":\"((?:\\\\.|[^\\\\\"])*)\"");
            java.util.regex.Matcher matcher = pattern.matcher(response.toString());
        
            if (matcher.find()) {
                String html = matcher.group(1)
                    .replaceAll("\\\\u003c", "<")
                    .replaceAll("\\\\u003e", ">")
                    .replace("\\\"", "\"");
                return html;
            }
            return null;
        }
    }
        int start = idx + 9;
        int end = response.indexOf("\"", start);
        if (end < 0) end = response.length();
        String html = response.substring(start, end).replaceAll("\\\\u003c", "<").replaceAll("\\\\u003e", ">")
                .replace("\\\"", "\"");
        return html;
    }
}
