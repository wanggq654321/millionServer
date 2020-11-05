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
import java.util.concurrent.atomic.AtomicLong;

@ChannelHandler.Sharable
public class TcpCountHandler extends ChannelInboundHandlerAdapter {

    public static AtomicInteger atomicInteger = new AtomicInteger();
    public Map<String, Channel> sessions = new ConcurrentHashMap<>();
    public static AtomicLong atomicIntegerQPS = new AtomicLong();
    long lastQ = 0;


    public TcpCountHandler() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long currentQ = atomicIntegerQPS.get();
            long qps = (long) ((currentQ - lastQ) / 5f);
            lastQ = currentQ;

            System.out.println("当前连接数为 = " + atomicInteger.get() + " sessions: " + sessions.size() + "  QPS: " + qps);
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 第三种
        // ctx.writeAndFlush(Unpooled.copiedBuffer("小滴课堂 xdclass.net", CharsetUtil.UTF_8));
//        ByteBuf data = (ByteBuf) msg;
//        System.out.println("服务端收到数据: " + data.toString(CharsetUtil.UTF_8));
        // ctx.fireChannelRead(data);   //调用下个handler

//        ByteBuf data = (ByteBuf) msg;
//        data.writeBytes("服务器返回心跳".getBytes());
//        ctx.writeAndFlush(data);

        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                ByteBuf data = (ByteBuf) msg;
                data.writeBytes("服务器返回心跳".getBytes());
                ctx.channel().writeAndFlush(data);
            }
        });
        atomicIntegerQPS.incrementAndGet();
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("TcpCountHandler exceptionCaught" + cause.toString());
        cause.printStackTrace();
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }
}
