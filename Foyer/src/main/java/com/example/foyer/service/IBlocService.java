package com.example.foyer.service;

import com.example.foyer.entities.Bloc;
import java.util.List;

public interface IBlocService {
    public List<Bloc> retrieveAllBlocs();
    public Bloc retrieveBloc(Long idBloc);
    public Bloc addBloc(Bloc bloc);
    public void removeBloc(Long idBloc);
    public Bloc modifyBloc(Bloc bloc);
}
