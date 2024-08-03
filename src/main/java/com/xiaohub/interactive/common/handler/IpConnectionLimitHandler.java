package com.xiaohub.interactive.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IpConnectionLimitHandler extends ChannelInboundHandlerAdapter {

    public static final Logger log = LoggerFactory.getLogger(IpConnectionLimitHandler.class);

    private final ConcurrentHashMap<String, AtomicInteger> ipCount = new ConcurrentHashMap<>();
    private final int maxConnections;

    public IpConnectionLimitHandler(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        ipCount.putIfAbsent(ip, new AtomicInteger(0));
        int currentCount = ipCount.get(ip).incrementAndGet();

        if (currentCount > maxConnections) {
            ctx.close();
            log.warn("Connection limit exceeded for IP: {}", ip);
        } else {
            super.channelActive(ctx);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        ipCount.get(ip).decrementAndGet();
        super.channelInactive(ctx);
    }
}
