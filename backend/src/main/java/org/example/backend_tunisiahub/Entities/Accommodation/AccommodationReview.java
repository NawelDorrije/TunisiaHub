package org.example.backend_tunisiahub.Entities.Accommodation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.User.User;
import java.time.LocalDate;
@Entity(name = "AccommodationReview")  // ← distinct name
@Table(name = "accommodation_review")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccommodationReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    int rating;              // 1 to 5

    @Column(nullable = false, length = 1000)
    String comment;

    @Column(nullable = false)
    LocalDate reviewDate;

    @ManyToOne
    @JoinColumn(name = "accommodation_id", nullable = false)
    @JsonIgnore
    Accommodation accommodation;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    User user;
}