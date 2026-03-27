package org.example.backend_tunisiahub.Services.Restaurant;

import org.example.backend_tunisiahub.Entities.Restaurant.MenuItem;

import java.util.List;

public interface IMenuItemService {

    List<MenuItem> retrieveAllMenuItems();

    MenuItem retrieveMenuItem(Long id);

    MenuItem addMenuItem(MenuItem menuItem);

    void deleteMenuItem(Long id);

    MenuItem modifyMenuItem(MenuItem menuItem);

    List<MenuItem> retrieveMenuItemsByMenuId(Long menuId);

}
