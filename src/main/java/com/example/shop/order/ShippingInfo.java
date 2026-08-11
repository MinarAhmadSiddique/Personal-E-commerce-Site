package com.example.shop.order;

public class ShippingInfo {
    private String name;
    private String line1;
    private String city;
    private String state;
    private String zip;

    public String name() { return name; }
    public String line1() { return line1; }
    public String city() { return city; }
    public String state() { return state; }
    public String zip() { return zip; }

    public void setName(String v) { this.name = v; }
    public void setLine1(String v) { this.line1 = v; }
    public void setCity(String v) { this.city = v; }
    public void setState(String v) { this.state = v; }
    public void setZip(String v) { this.zip = v; }
}
