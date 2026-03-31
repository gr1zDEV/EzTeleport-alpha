package com.ezinnovations.ezteleport.update;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GitHubReleaseClient {
    private static final Pattern TAG_NAME_PATTERN = Pattern.compile("\\\"tag_name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

    private final HttpClient httpClient;

    public GitHubReleaseClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build());
    }

    public GitHubReleaseClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CompletableFuture<Optional<String>> fetchLatestReleaseTag(String owner, String repo) {
        String encodedOwner = URLEncoder.encode(owner, StandardCharsets.UTF_8);
        String encodedRepo = URLEncoder.encode(repo, StandardCharsets.UTF_8);
        URI uri = URI.create("https://api.github.com/repos/" + encodedOwner + "/" + encodedRepo + "/releases/latest");

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "EzTeleport-UpdateChecker")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        return Optional.<String>empty();
                    }
                    return extractTagName(response.body());
                })
                .exceptionally(ex -> Optional.empty());
    }

    private Optional<String> extractTagName(String json) {
        Matcher matcher = TAG_NAME_PATTERN.matcher(json);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String tagName = matcher.group(1);
        if (tagName == null || tagName.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(unescapeJsonString(tagName));
    }

    private String unescapeJsonString(String value) {
        StringBuilder result = new StringBuilder(value.length());

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '\\' && i + 1 < value.length()) {
                char escaped = value.charAt(++i);
                switch (escaped) {
                    case '"' -> result.append('"');
                    case '\\' -> result.append('\\');
                    case '/' -> result.append('/');
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    default -> result.append(escaped);
                }
            } else {
                result.append(current);
            }
        }

        return result.toString();
    }
}
