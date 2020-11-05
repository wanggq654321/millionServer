package net.xdclass;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ChannelHandler.Sharable
public class TcpCountHandler extends ChannelInboundHandlerAdapter {

    public static AtomicInteger atomicInteger = new AtomicInteger();

    public Map<String, Channel> sessions = new ConcurrentHashMap<>();


    public TcpCountHandler() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("当前连接数为 = " + atomicInteger.get() + " sessions: " + sessions.size());
        }, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //第一种
        //Channel channel = ctx.channel();
        //channel.writeAndFlush(Unpooled.copiedBuffer("小滴课堂 xdclass.net",CharsetUtil.UTF_8));

        //第二种
        //ChannelPipeline channelPipeline = ctx.pipeline();
        //channelPipeline.writeAndFlush(Unpooled.copiedBuffer("小滴课堂 xdclass.net",CharsetUtil.UTF_8));

        //第三种
        ctx.writeAndFlush(Unpooled.copiedBuffer("小滴课堂 xdclass.net", CharsetUtil.UTF_8));

        ByteBuf data = (ByteBuf) msg;
        System.out.println("服务端收到数据: " + data.toString(CharsetUtil.UTF_8));
        // ctx.fireChannelRead(data);   //调用下个handler
        ctx.writeAndFlush(data);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        atomicInteger.incrementAndGet();
        sessions.put(ctx.channel().id().asLongText(), ctx.channel());
        // System.out.println("服务器地址：" + ctx.channel().localAddress() + "   客户端地址：" + ctx.channel().remoteAddress() + "  当前连接数为 = " + atomicInteger.get());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        atomicInteger.decrementAndGet();
        sessions.remove(ctx.channel().id().asLongText());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }
        IdleStateEvent idleEvent = (IdleStateEvent) evt;
        /*
         * 如果心跳请求发出30秒内没收到响应，则关闭连接
         */
        if (idleEvent.state().equals(IdleState.READER_IDLE) && ctx.channel().attr(AttributeKey.valueOf("heartbeat")) != null) {
            Long lastTime = (Long) ctx.channel().attr(AttributeKey.valueOf("heartbeat")).get();
            if (lastTime == null) lastTime = 0L;
            if (lastTime != null && System.currentTimeMillis() - lastTime >= 5 * 60 * 1000) {
                // LOGGER.info("userEventTriggered 服务器心跳超时 关闭链接 2222 ： " + (System.currentTimeMillis() - lastTime) / 1000 + "  秒:  " + ctx.toString());
                // ctx.channel().close();
            }
            ctx.channel().attr(AttributeKey.valueOf("heartbeat")).set(null);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("TcpCountHandler exceptionCaught" + cause.toString());
        cause.printStackTrace();
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }
}
