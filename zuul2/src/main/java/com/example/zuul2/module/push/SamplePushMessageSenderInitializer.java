package com.example.zuul2.module.push;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.zuul.netty.server.push.PushConnectionRegistry;
import com.netflix.zuul.netty.server.push.PushMessageSender;
import com.netflix.zuul.netty.server.push.PushMessageSenderInitializer;


@Singleton
public class SamplePushMessageSenderInitializer extends PushMessageSenderInitializer {

    private final PushMessageSender pushMessageSender;

    @Inject
    public SamplePushMessageSenderInitializer(PushConnectionRegistry pushConnectionRegistry) {
        super(pushConnectionRegistry);
        pushMessageSender = new SamplePushMessageSender(pushConnectionRegistry);
    }

    @Override
    protected PushMessageSender getPushMessageSender(PushConnectionRegistry pushConnectionRegistry) {
        return pushMessageSender;
    }

}