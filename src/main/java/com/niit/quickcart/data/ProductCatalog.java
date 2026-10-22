package com.niit.quickcart.data;

import com.niit.quickcart.model.Product;

import java.util.List;

public final class ProductCatalog {

    public static final List<Product> PRODUCTS = List.of(
            new Product(1, "Long-grain rice", "grocery", 8200, "🍚", "5 kg bag"),
            new Product(2, "Fresh tomatoes", "grocery", 1600, "🍅", "Ripe market tomatoes, 1 kg"),
            new Product(3, "Farm eggs", "grocery", 3200, "🥚", "One dozen"),
            new Product(4, "Jollof rice", "meal", 3500, "🍛", "Smoky party-style rice"),
            new Product(5, "Grilled chicken", "meal", 6200, "🍗", "Seasoned quarter chicken"),
            new Product(6, "Chilled zobo", "drink", 1000, "🥤", "Fresh hibiscus drink, 50 cl"),
            new Product(7, "Bottled water", "drink", 500, "💧", "Pure drinking water, 75 cl"),
            new Product(8, "Ripe avocados", "produce", 1800, "🥑", "Three fresh avocados"),
            new Product(9, "Coconut water", "drink", 1400, "🥥", "Chilled coconut water, 50 cl"),
            new Product(10, "Puff-puff box", "snack", 1800, "🍩", "A box of freshly made puff-puff"),
            new Product(11, "Sweet plantain", "produce", 1600, "🍌", "Ripe plantain, 1 kg"),
            new Product(12, "Agege bread", "bakery", 1200, "🍞", "Freshly baked local loaf"),
            new Product(13, "Chin-chin", "snack", 900, "🥨", "Crunchy homemade snack"),
            new Product(14, "Laundry detergent", "household", 2500, "🧺", "Everyday washing powder"),
            new Product(15, "Bathing soap", "personal", 1100, "🧼", "Gentle daily cleansing bar"),
            new Product(16, "Fresh bananas", "produce", 1300, "🍌", "A bunch of sweet bananas")
    );

    private ProductCatalog() {
    }

    public static Product findById(int id) {
        return PRODUCTS.stream().filter(product -> product.getId() == id).findFirst().orElse(null);
    }
}