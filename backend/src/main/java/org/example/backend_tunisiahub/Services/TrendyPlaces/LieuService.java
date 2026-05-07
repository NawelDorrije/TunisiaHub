package org.example.backend_tunisiahub.Services.TrendyPlaces;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.Lieu;
import org.example.backend_tunisiahub.Repositories.TrendyPlaces.LieuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LieuService implements ILieuService {

    private final LieuRepository lieuRepository;

    @Override
    public List<Lieu> retrieveAllLieux() {
        return lieuRepository.findAll();
    }

    @Override
    public Lieu retrieveLieu(Long id) {
        return lieuRepository.findById(id).orElse(null);
    }

    @Override
    public Lieu addLieu(Lieu lieu) {
        return lieuRepository.save(lieu);
    }

    @Override
    public Lieu updateLieu(Long id, Lieu lieuDetails) {
        Lieu lieu = lieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lieu not found"));
        lieu.setNom(lieuDetails.getNom());
        lieu.setDescription(lieuDetails.getDescription());
        lieu.setType(lieuDetails.getType());
        lieu.setVille(lieuDetails.getVille());
        lieu.setImage(lieuDetails.getImage());
        lieu.setLatitude(lieuDetails.getLatitude());
        lieu.setLongitude(lieuDetails.getLongitude());
        lieu.setHoraires(lieuDetails.getHoraires());
        return lieuRepository.save(lieu);
    }

    @Override
    public void deleteLieu(Long id) {
        lieuRepository.deleteById(id);
    }
}