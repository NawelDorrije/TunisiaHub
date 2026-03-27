package org.example.backend_tunisiahub.Controllers.Restaurant;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Restaurant.MenuItem;
import org.example.backend_tunisiahub.Services.Restaurant.IMenuItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final IMenuItemService menuItemService;

    @GetMapping
    public List<MenuItem> getAllMenuItems() {
        return menuItemService.retrieveAllMenuItems();
    }

    @GetMapping("/{id}")
    public MenuItem getMenuItemById(@PathVariable Long id) {
        return menuItemService.retrieveMenuItem(id);
    }

    @GetMapping("/by-menu/{menuId}")
    public List<MenuItem> getMenuItemsByMenuId(@PathVariable Long menuId) {
        return menuItemService.retrieveMenuItemsByMenuId(menuId);
    }

    @PostMapping("/add")
    public MenuItem createMenuItem(@RequestBody MenuItem menuItem) {
        return menuItemService.addMenuItem(menuItem);
    }

    @PutMapping("/update")
    public MenuItem updateMenuItem(@RequestBody MenuItem menuItem) {
        return menuItemService.modifyMenuItem(menuItem);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
    }
}
