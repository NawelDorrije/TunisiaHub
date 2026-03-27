package org.example.backend_tunisiahub.Services.Restaurant;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Restaurant.MenuItem;
import org.example.backend_tunisiahub.Repositories.Restaurant.MenuRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemService implements IMenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuRepository menuRepository;

    @Override
    public List<MenuItem> retrieveAllMenuItems() {
        return menuItemRepository.findAll();
    }

    @Override
    public MenuItem retrieveMenuItem(Long id) {
        return menuItemRepository.findById(id).orElse(null);
    }

    @Override
    public MenuItem addMenuItem(MenuItem menuItem) {
        attachMenuIfProvided(menuItem);
        return menuItemRepository.save(menuItem);
    }

    @Override
    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }

    @Override
    public MenuItem modifyMenuItem(MenuItem menuItem) {
        attachMenuIfProvided(menuItem);
        return menuItemRepository.save(menuItem);
    }

    @Override
    public List<MenuItem> retrieveMenuItemsByMenuId(Long menuId) {
        return menuItemRepository.findByMenu_Id(menuId);
    }

    private void attachMenuIfProvided(MenuItem menuItem) {
        if (menuItem.getMenu() == null && menuItem.getMenuId() != null) {
            menuItem.setMenu(menuRepository.findById(menuItem.getMenuId()).orElse(null));
        }
    }
}
