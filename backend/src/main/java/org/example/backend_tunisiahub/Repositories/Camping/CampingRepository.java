package org.example.backend_tunisiahub.Repositories.Camping;

import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampingRepository  extends JpaRepository<Camping,Long> {
}
