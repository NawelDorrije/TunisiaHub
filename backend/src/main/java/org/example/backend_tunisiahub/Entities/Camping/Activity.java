package org.example.backend_tunisiahub.Entities.Camping;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Column(length = 500)
    String description;

    @Column( nullable = false)

    BigDecimal price;

    Double duration;

    Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camping_id")
    Camping camping;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    Spot spot;
}