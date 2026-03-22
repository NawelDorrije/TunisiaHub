package com.example.foyer.service;

import com.example.foyer.entities.Chambre;
import com.example.foyer.entities.Foyer;
import com.example.foyer.entities.TypeChambre;

import java.util.List;

public interface IFoyerService {
    public List<Foyer> retrieveAllFoyers();
    public Foyer retrieveFoyer(Long idFoyer);
    public Foyer addFoyer(Foyer foyer);
    public void removeFoyer(Long idFoyer);
    public Foyer modifyFoyer(Foyer foyer);
    public Foyer retrieveFoyerByName(String nomFoyer);
    public Foyer retrieveFoyerByNameAndCapacity(String nomFoyer, long capaciteFoyer);

}
