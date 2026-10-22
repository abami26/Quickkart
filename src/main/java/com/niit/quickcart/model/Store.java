package com.niit.quickcart.model;

import java.util.List;

public class Store {

    private int id;
    private String name;
    private String category;
    private String icon;
    private List<String> serviceAreas;
    private List<Product> products;

    public Store() {
    }

    public Store(int id, String name, String category, String icon, List<String> serviceAreas, List<Product> products) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.icon = icon;
        this.serviceAreas = serviceAreas;
        this.products = products;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<String> getServiceAreas() {
        return serviceAreas;
    }

    public void setServiceAreas(List<String> serviceAreas) {
        this.serviceAreas = serviceAreas;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}