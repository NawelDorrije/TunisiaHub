package org.example.backend_tunisiahub.Services;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DatabaseMigrationService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationService.class);
    private final EntityManager entityManager;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateEnumValues() {
        try {
            // Fix CANCELED -> CANCELLED in reservation status
            int updatedReservations = entityManager.createNativeQuery(
                "UPDATE reservation SET status = 'CANCELLED' WHERE status = 'CANCELED'"
            ).executeUpdate();
            
            if (updatedReservations > 0) {
                logger.info("Migrated {} reservations: CANCELED -> CANCELLED", updatedReservations);
            }
            
            entityManager.flush();
        } catch (Exception e) {
            logger.warn("Error during database migration: {}", e.getMessage(), e);
        }
    }
}
