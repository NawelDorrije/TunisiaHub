package org.example.backend_tunisiahub.Services.TrendyPlaces;

import org.example.backend_tunisiahub.Entities.TrendyPlaces.Lieu;

import java.util.List;

public interface ILieuService {
    List<Lieu> retrieveAllLieux();
    Lieu retrieveLieu(Long id);
    Lieu addLieu(Lieu lieu);
    Lieu updateLieu(Long id, Lieu lieu);
    void deleteLieu(Long id);
}