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
                    List.of("Somolu", "Surulere", "Yaba"), products(4, 5, 6))
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