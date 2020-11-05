package net.xdclass;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;


public class NettyClient {


    // private static final String SERVER = "192.168.1.180";
    private static final String SERVER = "172.17.123.50";

    public static void main(String[] args) {
        new NettyClient().run(Config.BEGIN_PORT, Config.END_PORT);
    }


    /**
     * UDP
     *
     * @param beginPort
     * @param endPort
     */
    public void run(int beginPort, int endPort) {
        System.out.println("客户端启动中");
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        UdpClientHandler udpClientHandler = new UdpClientHandler();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(udpClientHandler);
        try {
            // 监听端口
            ChannelFuture sync = bootstrap.bind(0).sync();
            Channel udpChannel = sync.channel();
            String data = "我是大好人啊";
            udpChannel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data.getBytes(Charset.forName("UTF-8")))
                    , new InetSocketAddress("172.17.123.50", 8000)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }


//    /**
//     * TCP
//     * @param beginPort
//     * @param endPort
//     */
//    public void run(int beginPort, int endPort) {
//        System.out.println("客户端启动中");
//        EventLoopGroup group = new NioEventLoopGroup();
//        Bootstrap bootstrap = new Bootstrap();
//        TcpClientHandler echoClientHandler = new TcpClientHandler();
//        bootstrap.group(group)
//                .channel(NioSocketChannel.class)
//                .option(ChannelOption.SO_REUSEADDR, true)
//                .handler(new ChannelInitializer<SocketChannel>() {
//                    @Override
//                    protected void initChannel(SocketChannel ch) throws Exception {
//                        ch.pipeline().addLast(echoClientHandler);
//                        // System.out.println("服务器地址：" + ch.localAddress() + "   客户端地址：" + ch.remoteAddress());
//                    }
//                });
//
////        int index = 0;
////        int finalPort;
////        while (true) {
////            finalPort = beginPort + index;
////            try {
////                bootstrap.connect(SERVER, finalPort).addListener((ChannelFutureListener) future -> {
////                    if (!future.isSuccess()) {
////                        System.out.println("创建连接失败  " + future.cause().toString());
////                    }
////                }).get();
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////            ++index;
////            if (index == (endPort - beginPort)) {
////                index = 0;
////            }
////        }
//
//        for (int i = beginPort; i < endPort; i++) {
//            for (int j = 1025; j < 65536; j++) {
//                try {
//                    bootstrap.localAddress(j);
//                    bootstrap.connect(SERVER, i).addListener((ChannelFutureListener) future -> {
//                        if (!future.isSuccess()) {
//                            System.out.println("创建连接失败  " + future.cause().toString());
//                        }
//                    }).get();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }


}
