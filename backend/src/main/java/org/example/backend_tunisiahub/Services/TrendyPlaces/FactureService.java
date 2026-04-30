package org.example.backend_tunisiahub.Services.TrendyPlaces;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.ReservationActivite;
import org.example.backend_tunisiahub.Repositories.TrendyPlaces.ReservationActiviteRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("trendyPlacesFactureService")
@RequiredArgsConstructor
public class FactureService {

    private final ReservationActiviteRepository reservationRepo;

    public String genererToken(ReservationActivite r) {
        if (r.getFactureToken() == null) {
            r.setFactureToken(UUID.randomUUID().toString());
            r.setBilletUtilise(false);
            reservationRepo.save(r);
        }
        return r.getFactureToken();
    }

    public VerificationResult verifierBillet(String token) {
        return reservationRepo.findByFactureToken(token)
                .map(r -> {
                    if (!"CONFIRMEE".equals(r.getStatut())) {
                        return new VerificationResult(false, "INVALIDE",
                                "Cette reservation n'est pas confirmee.", null);
                    }
                    if (Boolean.TRUE.equals(r.getBilletUtilise())) {
                        return new VerificationResult(false, "DEJA_UTILISE",
                                "Ce billet a deja ete utilise.", r);
                    }
                    r.setBilletUtilise(true);
                    r.setDateBilletUtilise(new java.util.Date());
                    reservationRepo.save(r);
                    return new VerificationResult(true, "VALIDE",
                            "Billet valide - acces autorise !", r);
                })
                .orElse(new VerificationResult(false, "INTROUVABLE",
                        "Ce billet est introuvable.", null));
    }

    public record VerificationResult(
            boolean valide,
            String code,
            String message,
            ReservationActivite reservation
    ) {}
}
