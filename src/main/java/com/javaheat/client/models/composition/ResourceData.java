package com.javaheat.client.models.composition;

/**
 * Created by nle5220 on 08.11.2016.
 */
public class ResourceData <T> {
    Resource <T>  resource;

    public Resource <T> getResource() {
        return resource;
    }

    public void setResource(Resource <T> resource) {
        this.resource = resource;
    }

}
