package org.example.backend_tunisiahub.Services.Restaurant;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Restaurant.Menu;
import org.example.backend_tunisiahub.Repositories.Restaurant.MenuRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService implements IMenuService {

    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    public List<Menu> retrieveAllMenus() {
        return menuRepository.findAll();
    }

    @Override
    public Menu retrieveMenu(Long id) {
        return menuRepository.findById(id).orElse(null);
    }

    @Override
    public Menu addMenu(Menu menu) {
        attachRestaurantIfProvided(menu);
        return menuRepository.save(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }

    @Override
    public Menu modifyMenu(Menu menu) {
        attachRestaurantIfProvided(menu);
        return menuRepository.save(menu);
    }

    @Override
    public List<Menu> retrieveMenusByRestaurantId(Long restaurantId) {
        return menuRepository.findByRestaurant_Id(restaurantId);
    }

    private void attachRestaurantIfProvided(Menu menu) {
        if (menu.getRestaurant() == null && menu.getRestaurantId() != null) {
            menu.setRestaurant(restaurantRepository.findById(menu.getRestaurantId()).orElse(null));
        }
    }
}
