package org.example.backend_tunisiahub.Services.Accommodation;
<<<<<<< HEAD
=======
import org.example.backend_tunisiahub.Controllers.Accommodation.AccommodationStatsDTO;
>>>>>>> origin/feature/integrated-app-event
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import java.util.List;

public interface IAccommodationService {

<<<<<<< HEAD
    public List<Accommodation> retrieveAllAccommodations();

    public Accommodation retrieveAccommodation(Long accommodationId);

    public Accommodation addAccommodation(Accommodation accommodation);

    public void removeAccommodation(Long accommodationId);

    public Accommodation modifyAccommodation(Accommodation accommodation);
=======
  public List<Accommodation> retrieveAllAccommodations();

  public List<Accommodation> retrieveFilteredAccommodations(String type, Double minPrice, Double maxPrice, Integer minCapacity);

  public Accommodation retrieveAccommodation(Long accommodationId);

  public Accommodation addAccommodation(Accommodation accommodation);

  public void removeAccommodation(Long accommodationId);

  public Accommodation modifyAccommodation(Accommodation accommodation);
>>>>>>> origin/feature/integrated-app-event
}
