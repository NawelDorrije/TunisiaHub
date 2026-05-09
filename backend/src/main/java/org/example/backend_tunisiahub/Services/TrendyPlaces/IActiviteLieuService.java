package org.example.backend_tunisiahub.Services.TrendyPlaces;

import org.example.backend_tunisiahub.Entities.TrendyPlaces.ActiviteLieu;

import java.util.List;

public interface IActiviteLieuService {
    List<ActiviteLieu> retrieveAllActivites();
    List<ActiviteLieu> retrieveActivitesByLieu(Long lieuId);
    ActiviteLieu retrieveActivite(Long id);
    ActiviteLieu addActivite(ActiviteLieu activite, Long lieuId);
    ActiviteLieu updateActivite(Long id, ActiviteLieu activite, Long lieuId);
    void deleteActivite(Long id);
}