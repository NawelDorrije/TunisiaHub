package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
}
