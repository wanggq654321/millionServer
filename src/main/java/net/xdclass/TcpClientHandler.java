package net.xdclass;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@ChannelHandler.Sharable
public class TcpClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public static AtomicInteger atomicInteger = new AtomicInteger();
    long lastQ = 0;
    long startTime = 0;
    final long otherInter = 8 * 60 * 1000;

    public TcpClientHandler() {
        startTime = System.currentTimeMillis();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long currentQ = atomicInteger.get();
            long qps = (long) ((currentQ - lastQ) / 5f);
            lastQ = currentQ;
            System.out.println("数据量 = " + atomicInteger.get() + "  QPS: " + qps);
        }, 0, 5, TimeUnit.SECONDS);
    }

    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        // System.out.println("Client received: " + msg.toString(CharsetUtil.UTF_8));
        atomicInteger.incrementAndGet();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // System.out.println("Active");
        // ctx.writeAndFlush(Unpooled.copiedBuffer("小滴课堂 xdclass.net", CharsetUtil.UTF_8));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // System.out.println("TcpClientHandler channelReadComplete");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }
        IdleStateEvent idleEvent = (IdleStateEvent) evt;
        if (idleEvent.state().equals(IdleState.WRITER_IDLE) && ((System.currentTimeMillis() - startTime) > otherInter)) {
            ctx.channel().eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    ctx.channel().writeAndFlush(Unpooled.copiedBuffer("userEventTriggered 客户端心跳", CharsetUtil.UTF_8));
                }
            });
        }
//        /*
//         * 如果心跳请求发出30秒内没收到响应，则关闭连接
//         */
//        if (idleEvent.state().equals(IdleState.READER_IDLE) && ctx.channel().attr(AttributeKey.valueOf("heartbeat")) != null) {
//            Long lastTime = (Long) ctx.channel().attr(AttributeKey.valueOf("heartbeat")).get();
//            if (lastTime == null) lastTime = 0L;
//            if (lastTime != null && System.currentTimeMillis() - lastTime >= 5 * 60 * 1000) {
//                // LOGGER.info("userEventTriggered 服务器心跳超时 关闭链接 2222 ： " + (System.currentTimeMillis() - lastTime) / 1000 + "  秒:  " + ctx.toString());
//                // ctx.channel().close();
//            }
//            ctx.channel().attr(AttributeKey.valueOf("heartbeat")).set(null);
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }
}
