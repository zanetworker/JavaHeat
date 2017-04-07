package com.javaheat.client.models.authenticationv3;

import java.util.ArrayList;

public class CatalogItem {

    private ArrayList <EndpointItem> endpoints = new ArrayList<>();
    private String type;
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


 }
