package com.pollflow.adapter;

public interface ApiResponse<T> {
    boolean isSuccess();
    String getMessage();
    T getData();
}
