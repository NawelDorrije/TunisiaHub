package org.example.backend_tunisiahub.Entities.Accommodation;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.User.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    int rating;              // 1 to 5

    @Column(length = 1000)
    String comment;

    @Column(nullable = false)
    LocalDateTime submittedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "accommodation_id", nullable = false)
    Accommodation accommodation;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    org.example.backend_tunisiahub.Entities.Reservation reservation;

    @PrePersist
    public void prePersist() {
        this.submittedAt = LocalDateTime.now();
    }
}