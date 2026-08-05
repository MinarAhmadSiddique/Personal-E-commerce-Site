package com.example.shop.order;

public class PaymentFailedException extends RuntimeException{
    public PaymentFailedException(String message){
        super(message);
    }
}
