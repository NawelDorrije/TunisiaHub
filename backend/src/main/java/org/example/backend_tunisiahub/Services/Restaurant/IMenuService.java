package org.example.backend_tunisiahub.Services.Restaurant;

import org.example.backend_tunisiahub.Entities.Restaurant.Menu;

import java.util.List;

public interface IMenuService {

    List<Menu> retrieveAllMenus();

    Menu retrieveMenu(Long id);

    Menu addMenu(Menu menu);

    void deleteMenu(Long id);

    Menu modifyMenu(Menu menu);

    List<Menu> retrieveMenusByRestaurantId(Long restaurantId);

}
