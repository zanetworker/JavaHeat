package com.javaheat.client.models.composition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PortAttributes {

    private String name;
    private ArrayList<HashMap <String, String>> fixed_ips;
    private String mac_address;
    private String floating_ip;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public String getFloating_ip() {
        return floating_ip;
    }

    public void setFloating_ip(String floating_ip) {
        this.floating_ip = floating_ip;
    }


    public ArrayList<HashMap<String, String>> getFixed_ips() {
        return fixed_ips;
    }

    public void setFixed_ips(ArrayList<HashMap<String, String>> fixed_ips) {
        this.fixed_ips = fixed_ips;
    }
}
