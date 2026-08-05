package com.example.shop.order;

public class UnitUnavailableException extends RuntimeException{
    public UnitUnavailableException(String message){
        super(message);
    }
}
