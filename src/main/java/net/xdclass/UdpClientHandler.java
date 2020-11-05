package net.xdclass;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

@ChannelHandler.Sharable
public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        //利用ByteBuf的toString()方法获取请求消息
        String req = packet.content().toString(CharsetUtil.UTF_8);
        System.out.println(req);
//        //创建新的DatagramPacket对象，传入返回消息和目的地址（IP和端口）
//        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("谚语查询结果：", CharsetUtil.UTF_8), packet.sender()));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Active");
        // ctx.writeAndFlush(Unpooled.copiedBuffer("小滴课堂 xdclass.net", CharsetUtil.UTF_8));
        String data = "我是大好人啊";
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data.getBytes(Charset.forName("UTF-8")))
                , new InetSocketAddress("172.17.123.50", 8000)));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // System.out.println("TcpClientHandler channelReadComplete");
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
