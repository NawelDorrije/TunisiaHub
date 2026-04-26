package org.example.backend_tunisiahub.Entities.Camping;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Camping.Enums.EquipmentCondition;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Equipement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Column(length = 500)
    String description;

    @Column(nullable = false)
    Integer quantity;

    @Column(nullable = false)
    Boolean available = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_condition", nullable = false)
    EquipmentCondition condition;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    Spot spot;
}