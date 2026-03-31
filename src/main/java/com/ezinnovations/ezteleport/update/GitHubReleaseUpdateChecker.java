package com.ezinnovations.ezteleport.update;

import org.bukkit.plugin.java.JavaPlugin;

public final class GitHubReleaseUpdateChecker {
    private static final String GITHUB_OWNER = "ezinnovations";
    private static final String GITHUB_REPOSITORY = "EzTeleport-alpha";

    private final JavaPlugin plugin;
    private final GitHubReleaseClient releaseClient;

    public GitHubReleaseUpdateChecker(JavaPlugin plugin) {
        this(plugin, new GitHubReleaseClient());
    }

    public GitHubReleaseUpdateChecker(JavaPlugin plugin, GitHubReleaseClient releaseClient) {
        this.plugin = plugin;
        this.releaseClient = releaseClient;
    }

    public void checkForUpdatesOnStartup() {
        String currentVersion = normalizeVersion(plugin.getPluginMeta().getVersion());

        releaseClient.fetchLatestReleaseTag(GITHUB_OWNER, GITHUB_REPOSITORY)
                .thenAccept(optionalTag -> {
                    if (optionalTag.isEmpty()) {
                        plugin.getLogger().fine("Update check skipped: unable to get latest release from GitHub.");
                        return;
                    }

                    String latestVersion = normalizeVersion(optionalTag.get());
                    if (NumericVersionComparator.compare(currentVersion, latestVersion) < 0) {
                        plugin.getLogger().info("A new EzTeleport version is available: " + latestVersion
                                + " (installed: " + currentVersion + ").");
                    }
                });
    }

    static String normalizeVersion(String rawVersion) {
        if (rawVersion == null || rawVersion.isBlank()) {
            return "0";
        }

        return rawVersion.startsWith("v") || rawVersion.startsWith("V")
                ? rawVersion.substring(1).trim()
                : rawVersion.trim();
    }
}
