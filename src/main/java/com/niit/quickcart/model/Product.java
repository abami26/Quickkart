package com.niit.quickcart.model;

/**
 * A product in the catalog. Not stored in the database — see
 * {@link com.niit.quickcart.data.ProductCatalog} for the fixed list.
 * Field names match the JSON the frontend expects: id, name, cat, price, icon, desc.
 */
public class Product {

    private int id;
    private String name;
    private String cat;
    private int price;
    private String icon;
    private String desc;

    public Product() {
    }

    public Product(int id, String name, String cat, int price, String icon, String desc) {
        this.id = id;
        this.name = name;
        this.cat = cat;
        this.price = price;
        this.icon = icon;
        this.desc = desc;
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

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
