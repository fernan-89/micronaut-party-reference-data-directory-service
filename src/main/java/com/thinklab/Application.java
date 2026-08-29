package com.thinklab;

<<<<<<< HEAD
import com.mongodb.reactivestreams.client.MongoClient;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Main entry point for the Company Service.
 * Implements SRE practices for deterministic startup and fast-failure.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            Micronaut.build(args)
                    .mainClass(Application.class)
                    .start();
        } catch (Exception e) {
            log.error("[SRE] Fatal error during application startup. Fast-failing.", e);
            System.exit(1);
        }
    }

    /**
     * SRE Two-Phase Warmup: SDAM Topology Discovery barrier.
     * Executes a deterministic ping to MongoDB before allowing the application to be fully ready.
     * Fails fast if the database is unreachable, preventing HTTP traffic to a broken instance.
     */
    @Singleton
    public static class DatabaseWarmupBarrier implements ApplicationEventListener<StartupEvent> {

        private final MongoClient mongoClient;

        public DatabaseWarmupBarrier(MongoClient mongoClient) {
            this.mongoClient = mongoClient;
        }

        @Override
        public void onApplicationEvent(StartupEvent event) {
            log.info("[SRE] Initiating Two-Phase Warmup: MongoDB SDAM Topology Discovery...");

            try {
                // The blocking call is strictly intentional here. It acts as a synchronous barrier
                // during the startup phase, preventing the application from accepting requests
                // until the database connection pool is warmed up and verified.
                Document pingResult = Mono.from(mongoClient.getDatabase("admin").runCommand(new Document("ping", 1)))
                        .timeout(Duration.ofSeconds(2))
                        .block();

                log.info("[SRE] Two-Phase Warmup completed successfully. Database ping result: {}",
                        pingResult != null ? pingResult.toJson() : "null");

            } catch (Exception e) {
                log.error("[SRE] Two-Phase Warmup FAILED. MongoDB is unreachable. Halting application startup.", e);
                // Fails fast before readiness probes pass, preventing traffic routing to this pod
                System.exit(1);
            }
        }
=======
import io.micronaut.runtime.Micronaut;

public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
>>>>>>> 0fcdcb55c5340a3eba39b102c8d27c5dd0c7c9b0
    }
}