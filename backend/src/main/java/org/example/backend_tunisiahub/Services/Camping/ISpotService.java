package org.example.backend_tunisiahub.Services.Camping;

<<<<<<< HEAD
import org.example.backend_tunisiahub.Entities.Camping.Spot;

import java.util.List;

public interface ISpotService {

    List<Spot> retrieveAllSpots();

    Spot retrieveSpot(Long id);

    Spot addSpot(Spot spot);

    void deleteSpot(Long id);

    Spot modifySpot(Spot spot);
=======
import org.example.backend_tunisiahub.Entities.Camping.DTO.SpotDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotStatus;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotType;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ViewType;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ISpotService {

  SpotDTO createSpot(SpotDTO dto, List<MultipartFile> photos);
  SpotDTO updateSpot(Long id, SpotDTO dto, List<MultipartFile> newPhotos);
  void deleteSpot(Long id);
  List<SpotDTO> getAllSpots();
  Optional<SpotDTO> getSpotById(Long id);
  List<SpotDTO> getSpotsByCampingId(Long campingId);
  List<String> uploadPhotos(List<MultipartFile> files, String folder);

  // ── Filters ────────────────────────────────────────────
  List<SpotDTO> getByStatus(Long campingId, SpotStatus status);
  List<SpotDTO> getByType(Long campingId, SpotType type);
  List<SpotDTO> getByViewType(Long campingId, ViewType viewType);
  List<SpotDTO> getByAccessibility(Long campingId, Boolean accessibleForDisabled);
  List<SpotDTO> getByShade(Long campingId, Boolean hasShade);
  List<SpotDTO> getByPriceRange(Long campingId, BigDecimal min, BigDecimal max);
  List<SpotDTO> getByMinCapacity(Long campingId, Integer minCapacity);
  List<SpotDTO> getAvailableByDates(Long campingId, LocalDate checkIn, LocalDate checkOut);
  List<SpotDTO> getAvailableWithFilters(Long campingId, LocalDate checkIn, LocalDate checkOut,
                                        SpotType type, ViewType viewType, Boolean hasShade,
                                        Boolean accessibleForDisabled, Integer minCapacity,
                                        BigDecimal maxPrice);
>>>>>>> origin/feature/integrated-app-event
}
