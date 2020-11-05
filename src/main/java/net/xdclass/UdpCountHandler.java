package net.xdclass;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ChannelHandler.Sharable
public class UdpCountHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    public static AtomicInteger atomicInteger = new AtomicInteger();

    public UdpCountHandler() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            System.out.println("当前连接数为 = " + atomicInteger.get());
        }, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        //利用ByteBuf的toString()方法获取请求消息
        String req = packet.content().toString(CharsetUtil.UTF_8);
        System.out.println("Server received: " + req);
        //创建新的DatagramPacket对象，传入返回消息和目的地址（IP和端口）
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("谚语查询结果：", CharsetUtil.UTF_8), packet.sender()));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        atomicInteger.incrementAndGet();
        // System.err.println("ECHO active " + NioUdtProvider.socketUDT(ctx.channel()).toStringOptions());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        atomicInteger.decrementAndGet();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("TcpCountHandler exceptionCaught" + cause.toString());
        cause.printStackTrace();
        ctx.close();
    }
}
