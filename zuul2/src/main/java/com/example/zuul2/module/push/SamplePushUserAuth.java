package com.example.zuul2.module.push;

import com.netflix.zuul.netty.server.push.PushUserAuth;


public class SamplePushUserAuth implements PushUserAuth {

    private String customerId;
    private int statusCode;

    private SamplePushUserAuth(String customerId, int statusCode) {
        this.customerId = customerId;
        this.statusCode = statusCode;
    }

    // Successful auth
    public SamplePushUserAuth(String customerId) {
        this(customerId, 200);
    }

    // Failed auth
    public SamplePushUserAuth(int statusCode) {
        this("", statusCode);
    }

    @Override
    public boolean isSuccess() {
        return statusCode == 200;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String getClientIdentity() {
        return customerId;
    }
}