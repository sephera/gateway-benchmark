package com.example.zuul2.module.push;

import com.netflix.netty.common.channel.config.ChannelConfig;
import com.netflix.zuul.netty.server.ZuulDependencyKeys;
import com.netflix.zuul.netty.server.push.*;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;


public class SampleWebSocketPushChannelInitializer extends PushChannelInitializer {

    private final PushConnectionRegistry pushConnectionRegistry;
    private final PushAuthHandler pushAuthHandler;

    public SampleWebSocketPushChannelInitializer(
            String metricId, ChannelConfig channelConfig, ChannelConfig channelDependencies, ChannelGroup channels) {
        super(metricId, channelConfig, channelDependencies, channels);
        pushConnectionRegistry = channelDependencies.get(ZuulDependencyKeys.pushConnectionRegistry);
        pushAuthHandler = new SamplePushAuthHandler(PushProtocol.WEBSOCKET.getPath());
    }

    @Override
    protected void addPushHandlers(final ChannelPipeline pipeline) {
        pipeline.addLast(PushAuthHandler.NAME, pushAuthHandler);
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(PushProtocol.WEBSOCKET.getPath(), null, true));
        pipeline.addLast(new PushRegistrationHandler(pushConnectionRegistry, PushProtocol.WEBSOCKET));
        pipeline.addLast(new SampleWebSocketPushClientProtocolHandler());
    }
}
