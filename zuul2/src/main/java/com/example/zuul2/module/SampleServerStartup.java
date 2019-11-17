package com.example.zuul2.module;

import com.example.zuul2.module.push.SamplePushMessageSenderInitializer;
import com.example.zuul2.module.push.SampleSSEPushChannelInitializer;
import com.example.zuul2.module.push.SampleWebSocketPushChannelInitializer;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.config.DynamicIntProperty;
import com.netflix.discovery.EurekaClient;
import com.netflix.netty.common.accesslog.AccessLogPublisher;
import com.netflix.netty.common.channel.config.ChannelConfig;
import com.netflix.netty.common.channel.config.CommonChannelConfigKeys;
import com.netflix.netty.common.metrics.EventLoopGroupMetrics;
import com.netflix.netty.common.proxyprotocol.StripUntrustedProxyHeadersHandler;
import com.netflix.netty.common.ssl.ServerSslConfig;
import com.netflix.netty.common.status.ServerStatusManager;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.FilterUsageNotifier;
import com.netflix.zuul.RequestCompleteHandler;
import com.netflix.zuul.context.SessionContextDecorator;
import com.netflix.zuul.netty.server.*;
import com.netflix.zuul.netty.server.http2.Http2SslChannelInitializer;
import com.netflix.zuul.netty.server.push.PushConnectionRegistry;
import com.netflix.zuul.netty.ssl.BaseSslContextFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.ssl.ClientAuth;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Singleton
public class SampleServerStartup extends BaseServerStartup {

    enum ServerType {
        HTTP,
        HTTP2,
        HTTP_MUTUAL_TLS,
        WEBSOCKET,
        SSE
    }

    private static final String[] WWW_PROTOCOLS = new String[]{"TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3"};
    private static final ServerType SERVER_TYPE = ServerType.HTTP;
    private final PushConnectionRegistry pushConnectionRegistry;
    private final SamplePushMessageSenderInitializer pushSenderInitializer;

    @Inject
    public SampleServerStartup(ServerStatusManager serverStatusManager, FilterLoader filterLoader,
                               SessionContextDecorator sessionCtxDecorator, FilterUsageNotifier usageNotifier,
                               RequestCompleteHandler reqCompleteHandler, Registry registry,
                               DirectMemoryMonitor directMemoryMonitor, EventLoopGroupMetrics eventLoopGroupMetrics,
                               EurekaClient discoveryClient, ApplicationInfoManager applicationInfoManager,
                               AccessLogPublisher accessLogPublisher, PushConnectionRegistry pushConnectionRegistry,
                               SamplePushMessageSenderInitializer pushSenderInitializer) {
        super(serverStatusManager, filterLoader, sessionCtxDecorator, usageNotifier, reqCompleteHandler, registry,
                directMemoryMonitor, eventLoopGroupMetrics, discoveryClient, applicationInfoManager,
                accessLogPublisher);
        this.pushConnectionRegistry = pushConnectionRegistry;
        this.pushSenderInitializer = pushSenderInitializer;
    }

    @Override
    protected Map<SocketAddress, ChannelInitializer<?>> chooseAddrsAndChannels(ChannelGroup clientChannels) {
        Map<SocketAddress, ChannelInitializer<?>> addrsToChannels = new HashMap<>();

        String mainPortName = "main";
        int port = new DynamicIntProperty("zuul.server.port.main", 7001).get();
        SocketAddress sockAddr = new InetSocketAddress(port);

        ChannelConfig channelConfig = defaultChannelConfig(mainPortName);
        int pushPort = new DynamicIntProperty("zuul.server.port.http.push", 7008).get();
        ServerSslConfig sslConfig;
        ChannelConfig channelDependencies = defaultChannelDependencies(mainPortName);

        /* These settings may need to be tweaked depending if you're running behind an ELB HTTP listener, TCP listener,
         * or directly on the internet.
         */
        switch (SERVER_TYPE) {
            /* The below settings can be used when running behind an ELB HTTP listener that terminates SSL for you
             * and passes XFF headers.
             */
            case HTTP:
                channelConfig.set(CommonChannelConfigKeys.allowProxyHeadersWhen, StripUntrustedProxyHeadersHandler.AllowWhen.ALWAYS);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, false);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, false);

                addrsToChannels.put(
                        sockAddr,
                        new ZuulServerChannelInitializer(
                                String.valueOf(port), channelConfig, channelDependencies, clientChannels));
                logAddrConfigured(sockAddr);
                break;

            /* The below settings can be used when running behind an ELB TCP listener with proxy protocol, terminating
             * SSL in Zuul.
             */
            case HTTP2:
                sslConfig = ServerSslConfig.withDefaultCiphers(
                        loadFromResources("server.cert"),
                        loadFromResources("server.key"),
                        WWW_PROTOCOLS);

                channelConfig.set(CommonChannelConfigKeys.allowProxyHeadersWhen, StripUntrustedProxyHeadersHandler.AllowWhen.NEVER);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.serverSslConfig, sslConfig);
                channelConfig.set(CommonChannelConfigKeys.sslContextFactory, new BaseSslContextFactory(registry, sslConfig));

                addHttp2DefaultConfig(channelConfig, mainPortName);

                addrsToChannels.put(
                        sockAddr,
                        new Http2SslChannelInitializer(
                                String.valueOf(port), channelConfig, channelDependencies, clientChannels));
                logAddrConfigured(sockAddr, sslConfig);
                break;

            /* The below settings can be used when running behind an ELB TCP listener with proxy protocol, terminating
             * SSL in Zuul.
             *
             * Can be tested using certs in resources directory:
             *  curl https://localhost:7001/test -vk --cert src/main/resources/ssl/client.cert:zuul123 --key src/main/resources/ssl/client.key
             */
            case HTTP_MUTUAL_TLS:
                sslConfig = new ServerSslConfig(
                        WWW_PROTOCOLS,
                        ServerSslConfig.getDefaultCiphers(),
                        loadFromResources("server.cert"),
                        loadFromResources("server.key"),
                        null,
                        ClientAuth.REQUIRE,
                        loadFromResources("truststore.jks"),
                        loadFromResources("truststore.key"),
                        false);

                channelConfig.set(CommonChannelConfigKeys.allowProxyHeadersWhen, StripUntrustedProxyHeadersHandler.AllowWhen.NEVER);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, true);
                channelConfig.set(CommonChannelConfigKeys.serverSslConfig, sslConfig);
                channelConfig.set(CommonChannelConfigKeys.sslContextFactory, new BaseSslContextFactory(registry, sslConfig));

                addrsToChannels.put(
                        sockAddr,
                        new Http1MutualSslChannelInitializer(
                                String.valueOf(port), channelConfig, channelDependencies, clientChannels));
                logAddrConfigured(sockAddr, sslConfig);
                break;

            /* Settings to be used when running behind an ELB TCP listener with proxy protocol as a Push notification
             * server using WebSockets */
            case WEBSOCKET:
                channelConfig.set(CommonChannelConfigKeys.allowProxyHeadersWhen, StripUntrustedProxyHeadersHandler.AllowWhen.NEVER);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, true);

                channelDependencies.set(ZuulDependencyKeys.pushConnectionRegistry, pushConnectionRegistry);

                addrsToChannels.put(
                        sockAddr,
                        new SampleWebSocketPushChannelInitializer(
                                String.valueOf(port), channelConfig, channelDependencies, clientChannels));
                logAddrConfigured(sockAddr);

            {
                // port to accept push message from the backend, should be accessible on internal network only.
                SocketAddress pushSocketAddr = new InetSocketAddress(pushPort);
                addrsToChannels.put(pushSocketAddr, pushSenderInitializer);
                logAddrConfigured(pushSocketAddr);
            }

            break;

            /* Settings to be used when running behind an ELB TCP listener with proxy protocol as a Push notification
             * server using Server Sent Events (SSE) */
            case SSE:
                channelConfig.set(CommonChannelConfigKeys.allowProxyHeadersWhen, StripUntrustedProxyHeadersHandler.AllowWhen.NEVER);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, true);

                channelDependencies.set(ZuulDependencyKeys.pushConnectionRegistry, pushConnectionRegistry);

                addrsToChannels.put(
                        sockAddr,
                        new SampleSSEPushChannelInitializer(
                                String.valueOf(port), channelConfig, channelDependencies, clientChannels));
                logAddrConfigured(sockAddr);

            {
                SocketAddress pushSocketAddr = new InetSocketAddress(pushPort);
                // port to accept push message from the backend, should be accessible on internal network only.
                addrsToChannels.put(pushSocketAddr, pushSenderInitializer);
                logAddrConfigured(pushSocketAddr);
            }

            break;
        }

        return Collections.unmodifiableMap(addrsToChannels);
    }

    private File loadFromResources(String s) {
        return new File(ClassLoader.getSystemResource("ssl/" + s).getFile());
    }
}