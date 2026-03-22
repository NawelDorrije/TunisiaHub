package com.example.foyer.service;

import com.example.foyer.entities.Bloc;
import com.example.foyer.repository.BlocRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BlocServiceImpl implements IBlocService {

    private BlocRepository blocRepository;

    public List<Bloc> retrieveAllBlocs() {
        return blocRepository.findAll();
    }

    public Bloc retrieveBloc(Long idBloc) {
        return blocRepository.findById(idBloc).get();
    }

    public Bloc addBloc(Bloc bloc) {
        return blocRepository.save(bloc);
    }

    public void removeBloc(Long idBloc) {
        blocRepository.deleteById(idBloc);
    }

    public Bloc modifyBloc(Bloc bloc) {
        return blocRepository.save(bloc);
    }
}
