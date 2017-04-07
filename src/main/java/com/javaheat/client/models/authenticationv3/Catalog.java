package com.javaheat.client.models.authenticationv3;

import java.util.ArrayList;

public class Catalog {
    ArrayList <CatalogItem> catalogItems = new ArrayList<>();

    public ArrayList<CatalogItem> getCatalogItems() {
        return catalogItems;
    }

    public void setCatalogItems(ArrayList<CatalogItem> catalogItem) {
        this.catalogItems = catalogItem;
    }
}

