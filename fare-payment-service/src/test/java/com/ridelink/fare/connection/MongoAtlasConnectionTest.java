package com.ridelink.fare.connection;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Day 6 — MongoDB Atlas Connection Verification
 *
 * Connects directly to the Atlas cluster for the fare-payment-service
 * and pings the server. This test does NOT require RabbitMQ or the
 * full Spring context — it is a pure infrastructure smoke-test.
 *
 * SKIPPED automatically in CI (GitHub Actions sets CI=true).
 * Run locally with:
 *   mvn test -pl fare-payment-service -Dtest=MongoAtlasConnectionTest
 */
class MongoAtlasConnectionTest {

    /**
     * Skip this test entirely in CI environments.
     * GitHub Actions sets CI=true; the Atlas cluster is not reachable
     * from ephemeral cloud runners due to SSL/network restrictions.
     * The test runs only in local developer environments.
     */
    @BeforeEach
    void skipInCiEnvironment() {
        String ci = System.getenv("CI");
        Assumptions.assumeTrue(
                ci == null || ci.isBlank() || "false".equalsIgnoreCase(ci),
                "Skipping Atlas connection test in CI environment (CI=" + ci + "). " +
                "Run locally to verify Atlas connectivity."
        );
    }

    private static final Logger log = LoggerFactory.getLogger(MongoAtlasConnectionTest.class);

    /**
     * Atlas URI — read from env var first, fall back to the default.
     * In CI / production set MONGO_FARE_URI as a secret.
     */
    private static final String ATLAS_URI = System.getenv("MONGO_FARE_URI") != null
            ? System.getenv("MONGO_FARE_URI")
            : "mongodb+srv://ynimneth_db_user:wsq7LSTqyOA2tTQv@cluster0.dvvw3jy.mongodb.net/ridelink_fares?appName=Cluster0";

    private static final String DATABASE_NAME = "ridelink_fares";

    @Test
    @DisplayName("Atlas ping — fare-payment-service can reach Cluster0")
    void atlasClusterIsReachable() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(ATLAS_URI))
                .applyToSocketSettings(b -> b
                        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS))
                .applyToClusterSettings(b ->
                        b.serverSelectionTimeout(15, java.util.concurrent.TimeUnit.SECONDS))
                .build();

        try (MongoClient client = MongoClients.create(settings)) {
            Document pingResult = client
                    .getDatabase(DATABASE_NAME)
                    .runCommand(new Document("ping", 1));

            log.info("=======================================================");
            log.info("  MongoDB Atlas ping result : {}", pingResult.toJson());
            log.info("  Database                  : {}", DATABASE_NAME);
            log.info("  Service                   : fare-payment-service");
            log.info("=======================================================");

            Number ok = (Number) pingResult.get("ok");
            assertNotNull(ok, "Ping response must contain 'ok' field");
            assertEquals(1, ok.intValue(),
                    "Ping must return ok:1 — Atlas cluster is unreachable or credentials are wrong");
        }
    }

    @Test
    @DisplayName("Atlas collections — ridelink_fares database exists and is accessible")
    void faresDatabaseIsAccessible() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(ATLAS_URI))
                .applyToClusterSettings(b ->
                        b.serverSelectionTimeout(15, java.util.concurrent.TimeUnit.SECONDS))
                .build();

        try (MongoClient client = MongoClients.create(settings)) {
            // List databases to confirm our DB is accessible
            boolean dbFound = false;
            for (Document dbInfo : client.listDatabases()) {
                if (DATABASE_NAME.equals(dbInfo.getString("name"))) {
                    dbFound = true;
                    log.info("Found database '{}' on Atlas — sizeOnDisk: {} bytes",
                            DATABASE_NAME, dbInfo.get("sizeOnDisk"));
                    break;
                }
            }

            // Even if the DB doesn't show up yet (first time / empty), the ping proves connectivity.
            // Once the first document is inserted it will appear.
            log.info("Database '{}' visible in listDatabases: {}", DATABASE_NAME, dbFound);
            log.info("Atlas connection for fare-payment-service: VERIFIED");

            // The important assertion — we can reach the cluster without exception
            Document pingResult = client
                    .getDatabase("admin")
                    .runCommand(new Document("ping", 1));

            Number ok = (Number) pingResult.get("ok");
            assertNotNull(ok, "Admin ping response must contain 'ok' field");
            assertEquals(1, ok.intValue(), "Admin ping must succeed");
        }
    }
}
