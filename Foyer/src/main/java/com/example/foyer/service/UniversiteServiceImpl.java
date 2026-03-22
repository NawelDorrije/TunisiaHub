package com.example.foyer.service;

import com.example.foyer.entities.Foyer;
import com.example.foyer.entities.Universite;
import com.example.foyer.repository.FoyerRepository;
import com.example.foyer.repository.UniversiteRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UniversiteServiceImpl implements IUniversiteService {

    private UniversiteRepository universiteRepository;
    private FoyerRepository foyerRepository;

    public List<Universite> retrieveAllUniversites() {
        return universiteRepository.findAll();
    }

    public Universite retrieveUniversite(Long idUniversite) {
        return universiteRepository.findById(idUniversite).get();
    }

    public Universite addUniversite(Universite universite) {
        return universiteRepository.save(universite);
    }

    public void removeUniversite(Long idUniversite) {
        universiteRepository.deleteById(idUniversite);
    }

    public Universite modifyUniversite(Universite universite) {
        return universiteRepository.save(universite);
    }

    @Override
    public Universite addUniversiteAndFoyerAndAssign(Universite universite) {
        
        return universiteRepository.save(universite);
    }

    @Override
    public void assignFoyerToUniversite(Long universiteId, Long foyerId) {
        Universite universite = universiteRepository.findById(universiteId).get();
        Foyer foyer = foyerRepository.findById(foyerId).get();
        universite.setFoyer(foyer);
        universiteRepository.save(universite);
    }

    @Override
    public Universite addUniversiteAndAssignFoyerToUniversite(Universite universite, Long foyerId) {
        Foyer foyer = foyerRepository.findById(foyerId).get();
        universite.setFoyer(foyer);
        foyer.setUniversite(universite);
        return universiteRepository.save(universite);
    }

    @Override
    public Universite desaffecterFoyerFromUniversite(Long universiteId) {
        Universite universite = universiteRepository.findById(universiteId).get();
       
        universite.setFoyer(null);
        return universiteRepository.save(universite);
    }
}
