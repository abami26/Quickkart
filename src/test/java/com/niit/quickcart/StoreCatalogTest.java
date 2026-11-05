package com.niit.quickcart;

import com.niit.quickcart.data.ProductCatalog;
import com.niit.quickcart.data.StoreCatalog;
import com.niit.quickcart.model.Product;
import com.niit.quickcart.model.Store;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class StoreCatalogTest {

    @Test
    void findsStoresServingTheSelectedArea() {
        var stores = StoreCatalog.findByCity("ikeja");

        assertFalse(stores.isEmpty());
        assertEquals("Mama Nkechi's Store", stores.get(0).getName());
        assertFalse(stores.get(0).getProducts().isEmpty());
        stores.forEach(store -> assertTrue(store.getServiceAreas().contains("Ikeja")));
    }

    @Test
    void everyStoreProductExistsInTheProductCatalog() {
        StoreCatalog.STORES.forEach(store -> {
            assertFalse(store.getProducts().isEmpty(), store.getName() + " has no products");
            store.getProducts().forEach(product -> assertNotNull(product, store.getName() + " lists a missing product"));
        });
    }

    @Test
    void productIdsAreUniqueAndEachProductBelongsToOneStore() {
        var ids = ProductCatalog.PRODUCTS.stream().map(Product::getId).collect(Collectors.toList());
        assertEquals(ids.size(), new HashSet<>(ids).size(), "duplicate product ids");

        var storedIds = StoreCatalog.STORES.stream()
                .flatMap(store -> store.getProducts().stream())
                .map(Product::getId)
                .collect(Collectors.toList());
        assertEquals(storedIds.size(), new HashSet<>(storedIds).size(), "a product is sold by more than one store");
    }

    @Test
    void everyMainCategoryHasAtLeastSevenProducts() {
        for (String category : Set.of("meal", "bakery", "snack", "household", "personal")) {
            long count = ProductCatalog.PRODUCTS.stream().filter(p -> category.equals(p.getCat())).count();
            assertTrue(count >= 7, category + " has only " + count + " products");
        }
    }

    @Test
    void storeIdsAreUnique() {
        var ids = StoreCatalog.STORES.stream().map(Store::getId).collect(Collectors.toList());
        assertEquals(ids.size(), new HashSet<>(ids).size(), "duplicate store ids");
        assertTrue(ids.size() >= 10);
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