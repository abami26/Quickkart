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
            new Product(16, "Fresh bananas", "produce", 1300, "🍌", "A bunch of sweet bananas"),

            // ----- Bakery -----
            new Product(17, "Butter croissant", "bakery", 1500, "🥐", "Two flaky, freshly baked butter croissants"),
            new Product(18, "Sliced sandwich bread", "bakery", 1800, "🍞", "Soft family loaf, 600 g"),
            new Product(19, "Chocolate cake slice", "bakery", 2200, "🍰", "Rich, moist chocolate cake slice"),
            new Product(20, "Vanilla cupcakes", "bakery", 2800, "🧁", "Box of four iced vanilla cupcakes"),
            new Product(21, "Crusty baguette", "bakery", 1400, "🥖", "Oven-fresh baguette"),
            new Product(22, "Butter cookies", "bakery", 1600, "🍪", "Pack of 12 butter cookies"),
            new Product(23, "Glazed doughnuts", "bakery", 2400, "🍩", "Six sugar-glazed doughnuts"),

            // ----- Meals -----
            new Product(24, "Amala & ewedu", "meal", 3800, "🥘", "Amala with ewedu, gbegiri and assorted meat"),
            new Product(25, "Pounded yam & egusi", "meal", 4800, "🍲", "Pounded yam with egusi soup and beef"),
            new Product(26, "Fried rice & chicken", "meal", 4500, "🍛", "Fried rice, plantain and grilled chicken"),
            new Product(27, "Ofada rice & stew", "meal", 4200, "🍚", "Ofada rice with spicy ayamase stew"),
            new Product(28, "Moi moi", "meal", 1500, "🥣", "Two steamed bean pudding wraps"),
            new Product(29, "Goat meat pepper soup", "meal", 3800, "🍜", "Hot, spicy pepper soup with goat meat"),
            new Product(30, "Beef suya", "meal", 3000, "🍢", "Spicy grilled beef skewers with onions"),

            // ----- Snacks -----
            new Product(31, "Plantain chips", "snack", 900, "🍌", "Crunchy lightly salted plantain chips"),
            new Product(32, "Roasted groundnuts", "snack", 700, "🥜", "Crunchy roasted peanuts, 200 g"),
            new Product(33, "Meat pie", "snack", 1200, "🥧", "Flaky pastry filled with minced beef"),
            new Product(34, "Sausage roll", "snack", 1000, "🌭", "Freshly baked sausage roll"),
            new Product(35, "Kilishi", "snack", 2500, "🥩", "Spicy dried beef strips, 100 g"),
            new Product(36, "Popcorn tub", "snack", 1000, "🍿", "Lightly salted popcorn"),
            new Product(37, "Coconut candy", "snack", 600, "🥥", "Sweet, crunchy coconut candy"),

            // ----- Household -----
            new Product(38, "Dishwashing liquid", "household", 1400, "🧴", "Lemon dish soap, 750 ml"),
            new Product(39, "Household bleach", "household", 1300, "🧪", "Disinfecting bleach, 1 litre"),
            new Product(40, "Toilet tissue", "household", 2200, "🧻", "Soft tissue, 4 rolls"),
            new Product(41, "Floor cleaner", "household", 1800, "🧹", "Pine-scented floor cleaner, 1 litre"),
            new Product(42, "Refuse bags", "household", 1500, "🗑", "Strong bin liners, roll of 20"),
            new Product(43, "Kitchen sponges", "household", 900, "🧽", "Pack of 3 scouring sponges"),
            new Product(44, "Insecticide spray", "household", 2600, "🦟", "Mosquito and cockroach killer, 300 ml"),

            // ----- Personal care -----
            new Product(45, "Toothpaste", "personal", 1300, "🦷", "Fluoride toothpaste, 100 ml"),
            new Product(46, "Soft toothbrush", "personal", 700, "🪥", "Soft-bristle adult toothbrush"),
            new Product(47, "Body lotion", "personal", 2800, "🧴", "Moisturising body lotion, 400 ml"),
            new Product(48, "Hair cream", "personal", 2400, "💇", "Nourishing hair cream, 250 ml"),
            new Product(49, "Roll-on deodorant", "personal", 1500, "🌿", "48-hour roll-on deodorant, 50 ml"),
            new Product(50, "Hand sanitiser", "personal", 1200, "🧴", "Alcohol-based hand gel, 250 ml"),
            new Product(51, "Sanitary pads", "personal", 1800, "🌸", "Pack of 10 ultra-soft pads")
    );

    private ProductCatalog() {
    }

    public static Product findById(int id) {
        return PRODUCTS.stream().filter(product -> product.getId() == id).findFirst().orElse(null);
    }
}