package com.example.shop.catalog;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(String slug){
        super("No product for slug: "+slug);
    }
}
