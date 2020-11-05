package net.xdclass;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.TimeUnit;

public class NettyServer {

    public static void main(String[] args) {
        new NettyServer().run(Config.BEGIN_PORT, Config.END_PORT);
    }

    /**
     * TCP
     *
     * @param beginPort
     * @param endPort
     */
    public void run(int beginPort, int endPort) {
        System.out.println("服务端启动中");
        //配置服务端线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_REUSEADDR, true); //快速复用端口

        TcpCountHandler tcpCountHandler = new TcpCountHandler();
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                // System.out.println("服务器地址：" + ch.localAddress() + "   客户端地址：" + ch.remoteAddress());
                // ch.pipeline().addLast(new IdleStateHandler(180, 180, 0, TimeUnit.SECONDS));
                // ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                ch.pipeline().addLast(tcpCountHandler);
//                ch.pipeline().addLast(echoServerHandler);
            }
        });

        for (; beginPort < endPort; beginPort++) {
            int port = beginPort;
            serverBootstrap.bind(port).addListener((ChannelFutureListener) future -> {
                System.out.println("服务端成功绑定端口 port = " + port);
            });
        }
    }


//    /**
//     * UDP
//     *
//     * @param beginPort
//     * @param endPort
//     */
//    public void run(int beginPort, int endPort) {
//        System.out.println("服务端启动中");
//        //配置服务端线程组
//        UdpCountHandler udpCountHandler = new UdpCountHandler();
//        MultithreadEventLoopGroup group = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
//        Bootstrap bootstrap = new Bootstrap();
//        bootstrap.group(group)
//                .channel(Epoll.isAvailable() ? EpollDatagramChannel.class : NioDatagramChannel.class) // NioServerSocketChannel -> EpollDatagramChannel
//                .option(ChannelOption.SO_BROADCAST, true)
//                .option(EpollChannelOption.SO_REUSEPORT, true) // 配置EpollChannelOption.SO_REUSEPORT
//                .option(ChannelOption.SO_RCVBUF, 1024 * 1024 * 2)
//                .handler(udpCountHandler);
//        try {
//            if (Epoll.isAvailable()) {
//                // linux系统下使用SO_REUSEPORT特性，使得多个线程绑定同一个端口
//                int cpuNum = Runtime.getRuntime().availableProcessors();
//                for (int i = 0; i < cpuNum; i++) {
//                    ChannelFuture future = bootstrap.bind(8000).await();
//                    if (!future.isSuccess()) {
//                        System.out.println("服务端绑定端口fail port = " + 8000);
//                    } else {
//                        System.out.println("服务端成功绑定端口 port = " + 8000);
//                    }
//                }
//            } else {
//                ChannelFuture future = bootstrap.bind(8000).sync().channel().closeFuture().await();
//                if (!future.isSuccess()) {
//                    System.out.println("服务端绑定端口fail port = " + 8000);
//                } else {
//                    System.out.println("服务端成功绑定端口 port = " + 8000);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//
//        }
//
//    }


}
