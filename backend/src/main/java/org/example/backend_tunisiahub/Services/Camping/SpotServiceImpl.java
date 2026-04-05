package org.example.backend_tunisiahub.Services.Camping;

import com.cloudinary.Cloudinary;
import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Entities.Camping.DTO.SpotDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotStatus;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotType;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ViewType;
import org.example.backend_tunisiahub.Entities.Camping.Mappers.SpotMapper;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Repositories.Camping.CampingRepository;
import org.example.backend_tunisiahub.Repositories.Camping.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpotServiceImpl implements ISpotService {

    @Autowired private SpotRepository spotRepository;
    @Autowired private CampingRepository campingRepository;
    @Autowired private Cloudinary cloudinary;
    @Autowired private SpotMapper spotMapper;

    // ── CRUD ───────────────────────────────────────────────

    @Override
    public SpotDTO createSpot(SpotDTO dto, List<MultipartFile> photos) {
        Camping camping = campingRepository.findById(dto.getCampingId())
                .orElseThrow(() -> new RuntimeException("Camping not found with id: " + dto.getCampingId()));

        Spot spot = spotMapper.toEntity(dto);
        spot.setCamping(camping);

        if (photos != null && !photos.isEmpty()) {
            List<String> urls = uploadPhotos(photos, "campings/" + camping.getId() + "/spots");
            spot.setPhotos(urls);
        }

        return spotMapper.toDTO(spotRepository.save(spot));
    }

    @Override
    public SpotDTO updateSpot(Long id, SpotDTO dto, List<MultipartFile> newPhotos) {
        Spot existing = spotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spot not found with id: " + id));

        spotMapper.updateEntityFromDTO(dto, existing);

        if (newPhotos != null && !newPhotos.isEmpty()) {
            List<String> urls = uploadPhotos(newPhotos, "campings/" + existing.getCamping().getId() + "/spots");
            existing.getPhotos().addAll(urls);
        }

        return spotMapper.toDTO(spotRepository.save(existing));
    }

    @Override
    public void deleteSpot(Long id) {
        if (!spotRepository.existsById(id)) {
            throw new RuntimeException("Spot not found with id: " + id);
        }
        spotRepository.deleteById(id);
    }

    @Override
    public List<SpotDTO> getAllSpots() {
        return spotRepository.findAll()
                .stream().map(spotMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<SpotDTO> getSpotById(Long id) {
        return spotRepository.findById(id).map(spotMapper::toDTO);
    }

    @Override
    public List<SpotDTO> getSpotsByCampingId(Long campingId) {
        return spotRepository.findByCampingId(campingId)
                .stream().map(spotMapper::toDTO).collect(Collectors.toList());
    }

    // ── FILTERS ────────────────────────────────────────────

    @Override
    public List<SpotDTO> getByStatus(Long campingId, SpotStatus status) {
        return spotRepository.findByCampingIdAndStatus(campingId, status)
                .stream().map(spotMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<SpotDTO> getByType(Long campingId, SpotType type) {
        return spotRepository.findByCampingIdAndType(campingId, type)
                .stream().map(spotMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<SpotDTO> getByViewType(Long campingId, ViewType viewType) {
        return spotRepository.findByCampingIdAndViewType(campingId, viewType)
                .stream().map(spotMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<SpotDTO> getByAccessibility(Long campingId, Boolean accessibleForDisabled) {
        return spotRepository.findByCampingIdAndAccessibleForDisabled(campingId, accessibleForDisabled)
                .stream().map(spotMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<SpotDTO> getByShade(Long campingId, Boolean hasShade) {
        return spotRepository.findByCampingIdAndHasShade(campingId, hasShade)
                .stream().map(spotMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<SpotDTO> getByPriceRange(Long campingId, BigDecimal min, BigDecimal max) {
        return spotRepository.findByCampingIdAndBasePriceBetween(campingId, min, max)
                .stream().map(spotMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<SpotDTO> getByMinCapacity(Long campingId, Integer minCapacity) {
        return spotRepository.findByCampingIdAndCapacityGreaterThanEqual(campingId, minCapacity)
                .stream().map(spotMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<SpotDTO> getAvailableByDates(Long campingId, LocalDate checkIn, LocalDate checkOut) {
        return spotRepository.findAvailableSpotsByDates(campingId, checkIn, checkOut)
                .stream().map(spotMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<SpotDTO> getAvailableWithFilters(Long campingId, LocalDate checkIn, LocalDate checkOut,
                                                 SpotType type, ViewType viewType, Boolean hasShade,
                                                 Boolean accessibleForDisabled, Integer minCapacity,
                                                 BigDecimal maxPrice) {
        return spotRepository.findAvailableSpotsWithFilters(
                        campingId, checkIn, checkOut, type, viewType,
                        hasShade, accessibleForDisabled, minCapacity, maxPrice)
                .stream().map(spotMapper::toDTO).collect(Collectors.toList());
    }

    // ── UPLOAD ─────────────────────────────────────────────

    @Override
    public List<String> uploadPhotos(List<MultipartFile> files, String folder) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    Map uploadResult = cloudinary.uploader().upload(
                            file.getBytes(),
                            Map.of("folder", folder, "resource_type", "image")
                    );
                    urls.add((String) uploadResult.get("secure_url"));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload photo: " + file.getOriginalFilename(), e);
                }
            }
        }
        return urls;
    }
}