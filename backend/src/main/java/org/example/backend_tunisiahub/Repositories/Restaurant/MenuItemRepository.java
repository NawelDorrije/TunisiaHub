package org.example.backend_tunisiahub.Repositories.Restaurant;

import org.example.backend_tunisiahub.Entities.Restaurant.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByMenu_Id(Long menuId);
}
