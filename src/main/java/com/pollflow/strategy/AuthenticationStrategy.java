package com.pollflow.strategy;

public interface AuthenticationStrategy {
    boolean authenticate(String email, String password);
    String getToken(String email);
}
