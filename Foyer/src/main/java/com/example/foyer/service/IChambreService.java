package com.example.foyer.service;

import com.example.foyer.entities.Chambre;
import com.example.foyer.entities.TypeChambre;
import java.util.List;

public interface IChambreService {
    public List<Chambre> retrieveAllChambres();
    public Chambre retrieveChambre(Long chambreId);
    public Chambre addChambre(Chambre c);
    public void removeChambre(Long chambreId);
    public Chambre modifyChambre(Chambre chambre);
    public List<Chambre> retrieveChambresByTypeAndNumero(TypeChambre typeC, Long numeroChambre);
}
