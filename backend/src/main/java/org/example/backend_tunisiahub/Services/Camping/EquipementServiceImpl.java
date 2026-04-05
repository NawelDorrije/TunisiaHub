package org.example.backend_tunisiahub.Services.Camping;

import org.example.backend_tunisiahub.Entities.Camping.DTO.EquipementDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.EquipmentCondition;
import org.example.backend_tunisiahub.Entities.Camping.Equipement;
import org.example.backend_tunisiahub.Entities.Camping.Mappers.EquipementMapper;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Repositories.Camping.EquipementRepository;
import org.example.backend_tunisiahub.Repositories.Camping.SpotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipementServiceImpl implements IEquipementService {

    private final EquipementRepository equipementRepository;
    private final SpotRepository spotRepository;
    private final EquipementMapper equipementMapper;

    public EquipementServiceImpl(EquipementRepository equipementRepository,
                                 SpotRepository spotRepository,
                                 EquipementMapper equipementMapper) {
        this.equipementRepository = equipementRepository;
        this.spotRepository = spotRepository;
        this.equipementMapper = equipementMapper;
    }

    @Override
    public EquipementDTO createEquipement(EquipementDTO dto) {
        Spot spot = spotRepository.findById(dto.getSpotId())
                .orElseThrow(() -> new RuntimeException("Spot not found with id: " + dto.getSpotId()));

        Equipement equipement = new Equipement();
        equipementMapper.updateEntityFromDTO(dto, equipement);
        equipement.setSpot(spot);

        return equipementMapper.toDTO(equipementRepository.save(equipement));
    }

    @Override
    public EquipementDTO updateEquipement(Long id, EquipementDTO dto) {
        Equipement equipement = equipementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipement not found with id: " + id));

        equipementMapper.updateEntityFromDTO(dto, equipement);

        // Allow changing the spot on update
        if (dto.getSpotId() != null) {
            Spot spot = spotRepository.findById(dto.getSpotId())
                    .orElseThrow(() -> new RuntimeException("Spot not found with id: " + dto.getSpotId()));
            equipement.setSpot(spot);
        }

        return equipementMapper.toDTO(equipementRepository.save(equipement));
    }

    @Override
    public void deleteEquipement(Long id) {
        if (!equipementRepository.existsById(id)) {
            throw new RuntimeException("Equipement not found with id: " + id);
        }
        equipementRepository.deleteById(id);
    }

    @Override
    public EquipementDTO getEquipementById(Long id) {
        return equipementRepository.findById(id)
                .map(equipementMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Equipement not found with id: " + id));
    }

    @Override
    public List<EquipementDTO> getAllEquipements() {
        return equipementRepository.findAll()
                .stream().map(equipementMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EquipementDTO> getBySpotId(Long spotId) {
        return equipementRepository.findBySpotId(spotId)
                .stream().map(equipementMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EquipementDTO> getByAvailability(Boolean available) {
        return equipementRepository.findByAvailable(available)
                .stream().map(equipementMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EquipementDTO> getByCondition(EquipmentCondition condition) {
        return equipementRepository.findByCondition(condition)
                .stream().map(equipementMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EquipementDTO> getBySpotAndAvailability(Long spotId, Boolean available) {
        return equipementRepository.findBySpotIdAndAvailable(spotId, available)
                .stream().map(equipementMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EquipementDTO> getBySpotAndCondition(Long spotId, EquipmentCondition condition) {
        return equipementRepository.findBySpotIdAndCondition(spotId, condition)
                .stream().map(equipementMapper::toDTO).collect(Collectors.toList());
    }
}