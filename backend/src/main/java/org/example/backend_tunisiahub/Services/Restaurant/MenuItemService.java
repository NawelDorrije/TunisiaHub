package org.example.backend_tunisiahub.Services.Restaurant;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Restaurant.MenuItem;
import org.example.backend_tunisiahub.Repositories.Restaurant.MenuRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.MenuItemRepository;
import org.example.backend_tunisiahub.Services.MediaStorageService;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemService implements IMenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuRepository menuRepository;
    private final MediaStorageService mediaStorageService;

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
        normalizePrice(menuItem);
        normalizePicture(menuItem);
        attachMenuIfProvided(menuItem);
        return menuItemRepository.save(menuItem);
    }

    @Override
    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }

    @Override
    public MenuItem modifyMenuItem(MenuItem menuItem) {
        normalizePrice(menuItem);
        normalizePicture(menuItem);
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

    private void normalizePicture(MenuItem menuItem) {
        if (menuItem == null || !StringUtils.hasText(menuItem.getPicture())) {
            return;
        }

        String picture = menuItem.getPicture().trim();
        if (picture.startsWith("data:")) {
            menuItem.setPicture(mediaStorageService.storeMenuItemPicture(picture));
            return;
        }
        if (picture.length() > 2048) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Picture path is too long");
        }
    }

    private void normalizePrice(MenuItem menuItem) {
        if (menuItem == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Menu item payload is required");
        }
        if (menuItem.getPrice() == null) {
            menuItem.setPrice(BigDecimal.ZERO);
            return;
        }
        if (menuItem.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Price must be greater than or equal to 0");
        }
    }
}
