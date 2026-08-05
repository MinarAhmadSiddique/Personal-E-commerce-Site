package com.example.shop.user;

public class EmailAlreadyUsedException extends RuntimeException{
    public EmailAlreadyUsedException(String email){
        super("Email already registered: "+email);
    }
}
