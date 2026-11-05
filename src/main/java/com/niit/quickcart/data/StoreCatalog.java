package com.niit.quickcart.data;

import com.niit.quickcart.model.Product;
import com.niit.quickcart.model.Store;

import java.util.List;
import java.util.Locale;

public final class StoreCatalog {

    public static final List<Store> STORES = List.of(
            new Store(1, "Mama Nkechi's Store", "Groceries & essentials", "🛒",
                    List.of("Agege", "Alimosho", "Ikeja", "Kosofe"), products(1, 2, 3, 7, 11, 12, 13, 14, 15, 16)),
            new Store(2, "Lekki Fresh Market", "Market produce & drinks", "🥬",
                    List.of("Ajah", "Lekki", "Victoria Island"), products(8, 9, 10)),
            new Store(3, "Yaba Kitchen & Pantry", "Meals, groceries & drinks", "🍲",
                    List.of("Somolu", "Surulere", "Yaba"), products(4, 5, 6)),

            // Every product belongs to exactly one store, so a product never shows twice in the UI.
            new Store(4, "Crumbs & Crust Bakery", "Fresh bread, cakes & pastries", "🥐",
                    List.of("Ikeja", "Lekki", "Surulere", "Yaba"), products(17, 18, 19, 20, 21)),
            new Store(5, "Iya Basira's Kitchen", "Nigerian meals & soups", "🍲",
                    List.of("Agege", "Alimosho", "Ikeja", "Kosofe"), products(24, 25, 26, 27, 28)),
            new Store(6, "Snack Shack Lagos", "Snacks & small chops", "🍿",
                    List.of("Ajah", "Ikeja", "Kosofe", "Somolu"), products(31, 32, 33, 34, 35)),
            new Store(7, "HomeCare Supplies", "Household & cleaning supplies", "🧹",
                    List.of("Agege", "Alimosho", "Ikeja", "Surulere"), products(38, 39, 40, 41, 42)),
            new Store(8, "Glow Beauty & Care", "Personal care & toiletries", "💄",
                    List.of("Ikeja", "Lekki", "Victoria Island", "Yaba"), products(45, 46, 47, 48, 49)),
            new Store(9, "Surulere Corner Mart", "Snacks, bakery & home basics", "🏪",
                    List.of("Kosofe", "Somolu", "Surulere", "Yaba"), products(22, 23, 36, 37, 43)),
            new Store(10, "Island Express Mart", "Meals, personal care & more", "🛍",
                    List.of("Ajah", "Lekki", "Somolu", "Victoria Island"), products(29, 30, 44, 50, 51))
    );

    private StoreCatalog() {
    }

    public static List<Store> findByCity(String city) {
        if (city == null || city.isBlank()) {
            return List.of();
        }

        String requestedCity = city.trim().toLowerCase(Locale.ROOT);
        return STORES.stream()
                .filter(store -> store.getServiceAreas().stream()
                        .anyMatch(area -> area.toLowerCase(Locale.ROOT).equals(requestedCity)))
                .toList();
    }

    private static List<Product> products(int... ids) {
        return java.util.Arrays.stream(ids)
                .mapToObj(ProductCatalog::findById)
                .toList();
    }
}