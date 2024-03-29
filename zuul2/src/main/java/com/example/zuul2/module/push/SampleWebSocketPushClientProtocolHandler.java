package com.example.zuul2.module.push;

import com.netflix.zuul.netty.server.push.PushClientProtocolHandler;
import com.netflix.zuul.netty.server.push.PushProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SampleWebSocketPushClientProtocolHandler extends PushClientProtocolHandler {

    private static Logger logger = LoggerFactory.getLogger(SampleWebSocketPushClientProtocolHandler.class);

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (!isAuthenticated()) {
                // Do not entertain ANY message from unauthenticated client
                PushProtocol.WEBSOCKET.sendErrorAndClose(ctx, 1007, "Missing authentication");
            } else if (msg instanceof PingWebSocketFrame) {
                logger.debug("received ping frame");
                ctx.writeAndFlush(new PongWebSocketFrame());
            } else if (msg instanceof CloseWebSocketFrame) {
                logger.debug("received close frame");
                ctx.close();
            } else if (msg instanceof TextWebSocketFrame) {
                final TextWebSocketFrame tf = (TextWebSocketFrame) msg;
                final String text = tf.text();
                logger.debug("received test frame: {}", text);
                if (text != null && text.startsWith("ECHO ")) { //echo protocol
                    ctx.channel().writeAndFlush(tf.copy());
                }
            } else if (msg instanceof BinaryWebSocketFrame) {
                logger.debug("received binary frame");
                PushProtocol.WEBSOCKET.sendErrorAndClose(ctx, 1003, "Binary WebSocket frames not supported");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }


}