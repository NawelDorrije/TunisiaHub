package org.example.backend_tunisiahub.Services.Camping;

import org.example.backend_tunisiahub.Entities.Camping.Camping;

import java.util.List;

public interface ICampingService {

    List<Camping> retrieveAllCampings();

    Camping retrieveCamping(Long id);

    Camping addCamping(Camping camping);

    void deleteCamping(Long id);

    Camping modifyCamping(Camping camping);

}
