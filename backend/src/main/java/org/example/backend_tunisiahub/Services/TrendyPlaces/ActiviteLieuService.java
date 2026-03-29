package org.example.backend_tunisiahub.Services.TrendyPlaces;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.ActiviteLieu;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.Lieu;
import org.example.backend_tunisiahub.Repositories.TrendyPlaces.ActiviteLieuRepository;
import org.example.backend_tunisiahub.Repositories.TrendyPlaces.LieuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActiviteLieuService implements IActiviteLieuService {

    private final ActiviteLieuRepository activiteLieuRepository;
    private final LieuRepository lieuRepository;

    @Override
    public List<ActiviteLieu> retrieveAllActivites() {
        return activiteLieuRepository.findAll();
    }

    @Override
    public List<ActiviteLieu> retrieveActivitesByLieu(Long lieuId) {
        return activiteLieuRepository.findByLieuId(lieuId);
    }

    @Override
    public ActiviteLieu retrieveActivite(Long id) {
        return activiteLieuRepository.findById(id).orElse(null);
    }

    @Override
    public ActiviteLieu addActivite(ActiviteLieu activite, Long lieuId) {
        Lieu lieu = lieuRepository.findById(lieuId)
                .orElseThrow(() -> new RuntimeException("Lieu not found"));
        activite.setLieu(lieu);
        return activiteLieuRepository.save(activite);
    }

    @Override
    public ActiviteLieu updateActivite(Long id, ActiviteLieu activiteDetails, Long lieuId) {
        ActiviteLieu activite = activiteLieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activite not found"));
        activite.setNomActivite(activiteDetails.getNomActivite());
        activite.setDescription(activiteDetails.getDescription());
        activite.setPrix(activiteDetails.getPrix());
        activite.setDuree(activiteDetails.getDuree());
        activite.setCapaciteMax(activiteDetails.getCapaciteMax());
        activite.setDisponible(activiteDetails.getDisponible());
        Lieu lieu = lieuRepository.findById(lieuId)
                .orElseThrow(() -> new RuntimeException("Lieu not found"));
        activite.setLieu(lieu);
        return activiteLieuRepository.save(activite);
    }

    @Override
    public void deleteActivite(Long id) {
        activiteLieuRepository.deleteById(id);
    }
}