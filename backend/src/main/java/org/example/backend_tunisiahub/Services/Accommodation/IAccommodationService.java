package org.example.backend_tunisiahub.Services.Accommodation;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import java.util.List;

public interface IAccommodationService {

    public List<Accommodation> retrieveAllAccommodations();

    public Accommodation retrieveAccommodation(Long accommodationId);

    public Accommodation addAccommodation(Accommodation accommodation);

    public void removeAccommodation(Long accommodationId);

    public Accommodation modifyAccommodation(Accommodation accommodation);
}
