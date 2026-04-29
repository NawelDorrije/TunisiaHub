package org.example.backend_tunisiahub.Services.Camping;

import com.cloudinary.Cloudinary;
import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Entities.Camping.DTO.CampingDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.CampingStatus;
import org.example.backend_tunisiahub.Entities.Camping.Mappers.CampingMapper;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.Camping.CampingRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CampingServiceImpl implements ICampingService {

    @Autowired private CampingRepository campingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private Cloudinary cloudinary;
    @Autowired private CampingMapper campingMapper;

    // ── CRUD ───────────────────────────────────────────────

    @Override
    public CampingDTO createCamping(CampingDTO dto, List<MultipartFile> photos) {
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found with id: " + dto.getOwnerId()));

        Camping camping = campingMapper.toEntity(dto);
        camping.setOwner(owner);

        if (photos != null && !photos.isEmpty()) {
            List<String> urls = uploadPhotos(photos, "campings/" + owner.getId());
            camping.setPhotos(urls);
        }

        return campingMapper.toDTO(campingRepository.save(camping));
    }

    @Override
    public CampingDTO updateCamping(Long id, CampingDTO dto, List<MultipartFile> newPhotos) {
        Camping existing = campingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Camping not found with id: " + id));

        campingMapper.updateEntityFromDTO(dto, existing);
        existing.setUpdatedAt(LocalDateTime.now());

        if (newPhotos != null && !newPhotos.isEmpty()) {
            List<String> urls = uploadPhotos(newPhotos, "campings/" + existing.getOwner().getId());
            existing.getPhotos().addAll(urls);
        }

        return campingMapper.toDTO(campingRepository.save(existing));
    }

    @Override
    public void deleteCamping(Long id) {
        if (!campingRepository.existsById(id)) {
            throw new RuntimeException("Camping not found with id: " + id);
        }
        campingRepository.deleteById(id);
    }

    @Override
    public List<CampingDTO> getAllCampings() {
        LocalDate today = LocalDate.now();
        return campingRepository.findAvailableCampingsForDates(today)
                .stream()
                .map(campingMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CampingDTO> getCampingById(Long id) {
        return campingRepository.findById(id).map(campingMapper::toDTO);
    }

    // ── FILTERS ────────────────────────────────────────────

    @Override
    public List<CampingDTO> getByOwner(Long ownerId) {
        return campingRepository.findByOwnerId(ownerId)
                .stream().map(campingMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CampingDTO> getByStatus(CampingStatus status) {
        return campingRepository.findByStatus(status)
                .stream().map(campingMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CampingDTO> getByGovernorate(String governorate) {
        return campingRepository.findByGovernorate(governorate)
                .stream().map(campingMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CampingDTO> searchByKeyword(String keyword) {
        return campingRepository.searchByKeyword(keyword)
                .stream().map(campingMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CampingDTO> getAvailableByFilters(String governorate, Integer minCapacity) {
        return campingRepository.findAvailableByFilters(governorate, minCapacity)
                .stream().map(campingMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CampingDTO> getWithAvailableSpotsForDates(LocalDate checkIn, LocalDate checkOut, String governorate) {
        return campingRepository.findCampingsWithAvailableSpotsForDates(checkIn, checkOut, governorate)
                .stream().map(campingMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CampingDTO> getByMinRating(BigDecimal minRating) {
        return campingRepository.findByAverageRatingGreaterThanEqual(minRating)
                .stream().map(campingMapper::toDTO).collect(Collectors.toList());
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