package org.example.backend_tunisiahub.Repositories.Camping;

import org.example.backend_tunisiahub.Entities.Camping.PricingAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingAuditRepository extends JpaRepository<PricingAudit, Long> {

    List<PricingAudit> findBySpotIdOrderByCreatedAtDesc(Long spotId);

    List<PricingAudit> findTop10BySpotIdOrderByCreatedAtDesc(Long spotId);

    /**
     * Used by future ML feedback loop
     */
    List<PricingAudit> findByBookingConfirmedFalse();
}