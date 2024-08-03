package com.xiaohub.interactive.common.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Sharable
public class IpBasedTokenBucketLimiter extends ChannelInboundHandlerAdapter {

    public static final Logger log = LoggerFactory.getLogger(IpBasedTokenBucketLimiter.class);

    private final ConcurrentHashMap<String, TokenBucket> ipTokenBuckets;
    private final int maxTokens;
    private final int refillTokens;
    private final int refillInterval;

    /**
     * @param maxTokens      每秒可以处理最多 maxTokens 个请求
     * @param refillTokens   系统能够稳定地每秒处理最多 refillTokens 个请求
     * @param refillInterval 系统处理的时间间隔
     */
    public IpBasedTokenBucketLimiter(int maxTokens, int refillTokens, int refillInterval) {
        this.maxTokens = maxTokens;
        this.refillTokens = refillTokens;
        this.refillInterval = refillInterval;
        this.ipTokenBuckets = new ConcurrentHashMap<>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        TokenBucket tokenBucket = ipTokenBuckets.computeIfAbsent(ip, k -> new TokenBucket(maxTokens, refillTokens, refillInterval));

        if (tokenBucket.tryConsume()) {
            super.channelRead(ctx, msg);
        } else {
            log.warn("Request rate limit exceeded for IP: {}", ip);
            ctx.close();
        }
    }

    private static class TokenBucket {
        private final AtomicInteger availableTokens;
        private final int maxTokens;
        private final int refillToken;
        private final int refillInterval;
        private long lastRefillTimestamp;

        public TokenBucket(int maxTokens, int refillToken, int refillInterval) {
            this.availableTokens = new AtomicInteger(maxTokens);
            this.maxTokens = maxTokens;
            this.refillToken = refillToken;
            this.refillInterval = refillInterval;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now - lastRefillTimestamp > refillInterval) {
                int toAdd = (int) ((now - lastRefillTimestamp) / refillInterval * 60000) * refillToken;
                availableTokens.addAndGet(Math.min(toAdd, maxTokens - availableTokens.get()));
                lastRefillTimestamp = now;
            }

            return availableTokens.getAndIncrement() > 0;
        }
    }

}
