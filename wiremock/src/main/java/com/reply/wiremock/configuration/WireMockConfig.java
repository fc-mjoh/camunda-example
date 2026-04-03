package com.reply.wiremock.configuration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.net.URL;

@Configuration
public class WireMockConfig implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(WireMockConfig.class);

    @Value("${wiremock.port:9090}")
    private int wiremockPort;

    @Value("${wiremock.stubs-root:stubs}")
    private String stubsRoot;

    private WireMockServer wireMockServer;
    private volatile boolean running = false;

    /**
     * Creates the WireMockServer bean with the configured port, stubs directory,
     * and verbose request logging enabled.
     */
    @Bean
    public WireMockServer wireMockServer() {
        String resolvedRootDir = resolveStubsRootDirectory();

        LOGGER.info("Configuring WireMock server on port {} with stubs root: {}", wiremockPort, resolvedRootDir);

        WireMockConfiguration config = WireMockConfiguration.options()
                .port(wiremockPort)
                .withRootDirectory(resolvedRootDir)
                // Enable verbose request/response logging to the console
                .notifier(new ConsoleNotifier(true));

        wireMockServer = new WireMockServer(config);
        return wireMockServer;
    }

    /**
     * Resolves the stubs root directory. First checks the classpath for the
     * directory; if found, uses its absolute filesystem path. Otherwise, falls
     * back to treating the configured value as a relative or absolute filesystem path.
     */
    private String resolveStubsRootDirectory() {
        // Attempt to locate the stubs directory on the classpath
        URL classpathResource = getClass().getClassLoader().getResource(stubsRoot);
        if (classpathResource != null && "file".equals(classpathResource.getProtocol())) {
            String path = classpathResource.getPath();
            LOGGER.info("Found stubs directory on classpath: {}", path);
            return path;
        }

        // Fall back to filesystem path (relative to working directory or absolute)
        File filesystemDir = new File(stubsRoot);
        if (filesystemDir.exists() && filesystemDir.isDirectory()) {
            String absolutePath = filesystemDir.getAbsolutePath();
            LOGGER.info("Using stubs directory from filesystem: {}", absolutePath);
            return absolutePath;
        }

        // If nothing found, return the configured value as-is and let WireMock handle it
        LOGGER.warn("Stubs directory '{}' not found on classpath or filesystem. "
                + "WireMock will use it as-is; stubs may not load.", stubsRoot);
        return stubsRoot;
    }

    // -----------------------------------------------------------------------
    // SmartLifecycle implementation
    // -----------------------------------------------------------------------

    /**
     * Starts the WireMock server. Called automatically by Spring after the
     * application context is fully refreshed.
     */
    @Override
    public void start() {
        if (wireMockServer != null && !wireMockServer.isRunning()) {
            wireMockServer.start();
            running = true;

            LOGGER.info("==========================================================");
            LOGGER.info("  WireMock server started successfully");
            LOGGER.info("  Listening on port: {}", wiremockPort);
            LOGGER.info("  Admin UI:  http://localhost:{}/__admin", wiremockPort);
            LOGGER.info("  Stubs:     http://localhost:{}/<your-stub-url>", wiremockPort);
            LOGGER.info("  Stub mappings loaded: {}", wireMockServer.getStubMappings().size());
            LOGGER.info("==========================================================");
        }
    }

    /**
     * Stops the WireMock server. Called automatically by Spring during
     * application context shutdown.
     */
    @Override
    public void stop() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            LOGGER.info("Stopping WireMock server on port {}...", wiremockPort);
            wireMockServer.stop();
            running = false;
            LOGGER.info("WireMock server stopped.");
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns true so that Spring automatically calls start() after
     * context refresh.
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * Defines the phase for startup ordering. A higher value means this
     * component starts later (after other SmartLifecycle beans with lower phases)
     * and stops earlier during shutdown.
     */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    /**
     * Callback-based stop for graceful shutdown support.
     */
    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
}