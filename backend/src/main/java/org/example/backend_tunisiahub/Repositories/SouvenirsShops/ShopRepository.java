package org.example.backend_tunisiahub.Repositories.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByOwnerId(Long ownerId);
}
