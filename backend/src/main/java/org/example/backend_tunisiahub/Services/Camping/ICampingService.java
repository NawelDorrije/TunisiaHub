package org.example.backend_tunisiahub.Services.Camping;

import org.example.backend_tunisiahub.Entities.Camping.DTO.CampingDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.CampingStatus;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ICampingService {

    CampingDTO createCamping(CampingDTO dto, List<MultipartFile> photos);
    CampingDTO updateCamping(Long id, CampingDTO dto, List<MultipartFile> newPhotos);
    void deleteCamping(Long id);
    List<CampingDTO> getAllCampings();
    Optional<CampingDTO> getCampingById(Long id);
    List<String> uploadPhotos(List<MultipartFile> files, String folder);

    // ── Filters ────────────────────────────────────────────
    List<CampingDTO> getByOwner(Long ownerId);
    List<CampingDTO> getByStatus(CampingStatus status);
    List<CampingDTO> getByGovernorate(String governorate);
    List<CampingDTO> searchByKeyword(String keyword);
    List<CampingDTO> getAvailableByFilters(String governorate, Integer minCapacity);
    List<CampingDTO> getWithAvailableSpotsForDates(LocalDate checkIn, LocalDate checkOut, String governorate);
    List<CampingDTO> getByMinRating(BigDecimal minRating);
}