package com.niit.quickcart;

import com.niit.quickcart.data.StoreCatalog;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class StoreCatalogTest {

    @Test
    void findsStoresServingTheSelectedArea() {
        var stores = StoreCatalog.findByCity("ikeja");

        assertEquals(1, stores.size());
        assertEquals("Mama Nkechi's Store", stores.get(0).getName());
        assertFalse(stores.get(0).getProducts().isEmpty());
    }

    @Test
    void returnsNoStoresForAnUnservedArea() {
        assertTrue(StoreCatalog.findByCity("Epe").isEmpty());
    }

    @Test
    void everyLandingPageCategoryHasAvailableProducts() {
        var categories = StoreCatalog.STORES.stream()
                .flatMap(store -> store.getProducts().stream())
                .map(product -> product.getCat())
                .collect(Collectors.toSet());

        assertTrue(categories.containsAll(Set.of(
                "grocery", "produce", "meal", "bakery", "drink", "snack", "household", "personal")));
    }
}