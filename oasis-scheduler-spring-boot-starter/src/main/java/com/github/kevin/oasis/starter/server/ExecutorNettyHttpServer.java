package com.github.kevin.oasis.starter.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevin.oasis.starter.autoconfigure.OasisSchedulerProperties;
import com.github.kevin.oasis.starter.model.ExecutorInvokeRequest;
import com.github.kevin.oasis.starter.service.OasisExecutionService;
import com.github.kevin.oasis.starter.util.SignatureUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内置 Netty HTTP 服务，接收 admin 的 /invoke 请求并转发到执行线程池。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutorNettyHttpServer {

    private static final String HEADER_APP_CODE = "X-Oasis-App-Code";
    private static final String HEADER_TIMESTAMP = "X-Oasis-Timestamp";
    private static final String HEADER_NONCE = "X-Oasis-Nonce";
    private static final String HEADER_SIGNATURE = "X-Oasis-Signature";

    private final OasisSchedulerProperties properties;
    private final OasisExecutionService executionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    /**
     * key: appCode:nonce, value: firstSeenMillis
     */
    private final ConcurrentHashMap<String, Long> nonceCache = new ConcurrentHashMap<>();

    public synchronized boolean start() {
        if (!properties.isEnabled()) {
            return false;
        }
        if (serverChannel != null && serverChannel.isActive()) {
            return true;
        }

        try {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(Math.max(2, Runtime.getRuntime().availableProcessors()));

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(1024 * 1024));
                            pipeline.addLast(new InvokeHandler());
                        }
                    });

            ChannelFuture bindFuture = bootstrap.bind(properties.getServer().getPort()).sync();
            serverChannel = bindFuture.channel();
            log.info("oasis netty invoke server started, port={}, path={}/invoke",
                    properties.getServer().getPort(), normalizeContextPath(properties.getServer().getContextPath()));
            return true;
        } catch (Exception e) {
            log.error("start netty invoke server failed", e);
            stop();
            return false;
        }
    }

    public synchronized void stop() {
        if (serverChannel != null) {
            serverChannel.close().awaitUninterruptibly();
            serverChannel = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().awaitUninterruptibly();
            workerGroup = null;
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().awaitUninterruptibly();
            bossGroup = null;
        }
    }

    private String normalizeContextPath(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String path = value.startsWith("/") ? value : "/" + value;
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private final class InvokeHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
            String expectedPath = normalizeContextPath(properties.getServer().getContextPath()) + "/invoke";
            String requestPath = new QueryStringDecoder(req.uri()).path();

            // 只接受 POST /invoke，其他请求直接拒绝，避免未定义入口被误调用。
            if (!HttpMethod.POST.equals(req.method())) {
                writeJson(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED, Map.of("accepted", false, "message", "method not allowed"));
                return;
            }
            if (!expectedPath.equals(requestPath)) {
                writeJson(ctx, HttpResponseStatus.NOT_FOUND, Map.of("accepted", false, "message", "not found"));
                return;
            }

            try {
                String body = req.content().toString(CharsetUtil.UTF_8);

                // 对 admin 下发请求做签名校验，防止伪造触发请求直接打到执行器。
                if (!verifyInvokeSignature(req, expectedPath, body)) {
                    writeJson(ctx, HttpResponseStatus.UNAUTHORIZED, Map.of("accepted", false, "message", "invalid signature"));
                    return;
                }

                ExecutorInvokeRequest invokeRequest = objectMapper.readValue(body, ExecutorInvokeRequest.class);
                validateInvokeRequest(invokeRequest);

                // 下发请求只做快速入队，真正执行在 OasisExecutionService 线程池中异步完成。
                boolean accepted = executionService.submit(invokeRequest);
                writeJson(ctx, HttpResponseStatus.OK, Map.of("accepted", accepted, "fireLogId", invokeRequest.getFireLogId()));
            } catch (IllegalArgumentException e) {
                writeJson(ctx, HttpResponseStatus.BAD_REQUEST, Map.of("accepted", false, "message", e.getMessage()));
            } catch (Exception e) {
                log.warn("handle invoke request failed, msg={}", e.getMessage());
                writeJson(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, Map.of("accepted", false, "message", "internal error"));
            }
        }

        private boolean verifyInvokeSignature(FullHttpRequest req, String path, String body) {
            if (!properties.getServer().isVerifyInvokeSignature()) {
                return true;
            }

            String appCode = req.headers().get(HEADER_APP_CODE);
            String timestampText = req.headers().get(HEADER_TIMESTAMP);
            String nonce = req.headers().get(HEADER_NONCE);
            String signature = req.headers().get(HEADER_SIGNATURE);
            if (isBlank(appCode) || isBlank(timestampText) || isBlank(nonce) || isBlank(signature)) {
                return false;
            }

            if (!properties.getAppCode().equals(appCode)) {
                return false;
            }

            long timestamp;
            try {
                timestamp = Long.parseLong(timestampText);
            } catch (NumberFormatException e) {
                return false;
            }

            long now = System.currentTimeMillis();
            if (Math.abs(now - timestamp) > properties.getSecurity().getClockSkewMs()) {
                return false;
            }

            if (!checkAndRecordNonce(appCode, nonce, now)) {
                return false;
            }

            String expected = SignatureUtil.sign(
                    properties.getAppKey(),
                    timestamp,
                    nonce,
                    req.method().name(),
                    path,
                    body
            );
            return SignatureUtil.constantTimeEquals(expected, signature);
        }

        private boolean checkAndRecordNonce(String appCode, String nonce, long now) {
            String nonceKey = appCode + ":" + nonce;
            long ttl = properties.getSecurity().getNonceExpireMs();
            Long exist = nonceCache.putIfAbsent(nonceKey, now);
            if (exist != null) {
                if (now - exist <= ttl) {
                    return false;
                }
                boolean replaced = nonceCache.replace(nonceKey, exist, now);
                if (!replaced) {
                    return false;
                }
            }

            if (nonceCache.size() > properties.getSecurity().getNonceMaxSize()) {
                cleanupExpiredNonce(now, ttl);
            }
            return true;
        }

        private void cleanupExpiredNonce(long now, long ttl) {
            Iterator<Map.Entry<String, Long>> it = nonceCache.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> entry = it.next();
                if (entry.getValue() == null || now - entry.getValue() > ttl) {
                    it.remove();
                }
            }
        }

        private boolean isBlank(String value) {
            return value == null || value.isBlank();
        }

        private void validateInvokeRequest(ExecutorInvokeRequest request) {
            if (request == null) {
                throw new IllegalArgumentException("request body required");
            }
            if (request.getFireLogId() == null) {
                throw new IllegalArgumentException("fireLogId required");
            }
            if (request.getHandlerName() == null || request.getHandlerName().isBlank()) {
                throw new IllegalArgumentException("handlerName required");
            }
        }

        private void writeJson(ChannelHandlerContext ctx, HttpResponseStatus status, Map<String, Object> data) {
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(data);
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        status,
                        Unpooled.wrappedBuffer(bytes)
                );
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
                response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } catch (Exception e) {
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR))
                        .addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
