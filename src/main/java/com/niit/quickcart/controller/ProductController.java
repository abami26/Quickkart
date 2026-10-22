package com.niit.quickcart.controller;

import com.niit.quickcart.data.ProductCatalog;
import com.niit.quickcart.data.StoreCatalog;
import com.niit.quickcart.model.Product;
import com.niit.quickcart.model.Store;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

    @GetMapping("/api/products")
    public List<Product> getProducts() {
        return ProductCatalog.PRODUCTS;
    }

    @GetMapping("/api/stores")
    public List<Store> getStores(@RequestParam String city) {
        return StoreCatalog.findByCity(city);
    }
}
