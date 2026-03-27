package org.example.backend_tunisiahub.Controllers.Restaurant;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Restaurant.Menu;
import org.example.backend_tunisiahub.Entities.Restaurant.MenuType;
import org.example.backend_tunisiahub.Services.Restaurant.IMenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final IMenuService menuService;

    @GetMapping
    public List<Menu> getAllMenus() {
        return menuService.retrieveAllMenus();
    }

    @GetMapping("/types")
    public MenuType[] getMenuTypes() {
        return MenuType.values();
    }

    @GetMapping("/get/{id}")
    public Menu getMenuById(@PathVariable Long id) {
        return menuService.retrieveMenu(id);
    }

    @GetMapping("/by-restaurant/{restaurantId}")
    public List<Menu> getMenusByRestaurantId(@PathVariable Long restaurantId) {
        return menuService.retrieveMenusByRestaurantId(restaurantId);
    }

    @PostMapping("/add")
    public Menu createMenu(@RequestBody Menu menu) {
        return menuService.addMenu(menu);
    }

    @PutMapping("/update")
    public Menu updateMenu(@RequestBody Menu menu) {
        return menuService.modifyMenu(menu);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
    }
}
