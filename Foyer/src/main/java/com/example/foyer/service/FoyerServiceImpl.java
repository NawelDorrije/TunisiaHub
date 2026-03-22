package com.example.foyer.service;

import com.example.foyer.entities.Foyer;
import com.example.foyer.repository.FoyerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FoyerServiceImpl implements IFoyerService {

    private FoyerRepository foyerRepository;

    public List<Foyer> retrieveAllFoyers() {
        return foyerRepository.findAll();
    }

    public Foyer retrieveFoyer(Long idFoyer) {
        return foyerRepository.findById(idFoyer).get();
    }

    public Foyer addFoyer(Foyer foyer) {
        return foyerRepository.save(foyer);
    }

    public void removeFoyer(Long idFoyer) {
        foyerRepository.deleteById(idFoyer);
    }

    public Foyer modifyFoyer(Foyer foyer) {
        return foyerRepository.save(foyer);
    }

    public Foyer retrieveFoyerByName(String nomFoyer) {
        return foyerRepository.findByNomFoyer(nomFoyer);
    }

    public Foyer retrieveFoyerByNameAndCapacity(String nomFoyer, long capaciteFoyer) {
        return foyerRepository.findByNomFoyerAndCapaciteFoyerLessThan(nomFoyer, capaciteFoyer);
    }
}
