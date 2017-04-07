package com.javaheat.client.models.authenticationv3;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by nle5220 on 07.04.2017.
 */
public class EndpointItem {
    @JsonProperty("interface")
    String iface;
    String url;
    String id;

    public String getIface() {
        return iface;
    }

    public void setIface(String iface) {
        this.iface = iface;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
