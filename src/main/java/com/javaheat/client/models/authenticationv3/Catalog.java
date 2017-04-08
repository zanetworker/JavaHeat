package com.javaheat.client.models.authenticationv3;

import java.util.ArrayList;

public class Catalog {
    String name;
    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
//    ArrayList <CatalogItem> catalogItems = new ArrayList<>();

//    public ArrayList<CatalogItem> getCatalogItems() {
//        return catalogItems;
//    }
//
//    public void setCatalogItems(ArrayList<CatalogItem> catalogItem) {
//        this.catalogItems = catalogItem;
//    }
}

