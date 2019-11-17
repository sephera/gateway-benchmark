package com.example.zuul2.module.push;

import com.google.common.base.Strings;
import com.netflix.zuul.message.http.Cookies;
import com.netflix.zuul.netty.server.push.PushAuthHandler;
import com.netflix.zuul.netty.server.push.PushUserAuth;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;


@ChannelHandler.Sharable
public class SamplePushAuthHandler extends PushAuthHandler {

    public SamplePushAuthHandler(String path) {
        super(path, ".sample.netflix.com");
    }

    @Override
    protected boolean isDelayedAuth(FullHttpRequest req, ChannelHandlerContext ctx) {
        return false;
    }

    @Override
    protected PushUserAuth doAuth(FullHttpRequest req) {
        final Cookies cookies = parseCookies(req);
        for (final Cookie c : cookies.getAll()) {
            if (c.getName().equals("userAuthCookie")) {
                final String customerId = c.getValue();
                if (!Strings.isNullOrEmpty(customerId)) {
                    return new SamplePushUserAuth(customerId);
                }
            }
        }
        return new SamplePushUserAuth(HttpResponseStatus.UNAUTHORIZED.code());
    }

}