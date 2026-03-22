package com.example.foyer.service;

import com.example.foyer.entities.Universite;
import java.util.List;

public interface IUniversiteService {
    public List<Universite> retrieveAllUniversites();
    public Universite retrieveUniversite(Long idUniversite);
    public Universite addUniversite(Universite universite);
    public void removeUniversite(Long idUniversite);
    public Universite modifyUniversite(Universite universite);

    Universite addUniversiteAndFoyerAndAssign(Universite universite);
    void assignFoyerToUniversite(Long universiteId, Long foyerId);
    Universite addUniversiteAndAssignFoyerToUniversite(Universite universite, Long foyerId);
    Universite desaffecterFoyerFromUniversite(Long universiteId);
}
