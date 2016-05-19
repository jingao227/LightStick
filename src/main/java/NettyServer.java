import java.util.HashMap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;

public class NettyServer {

    private static HashMap<Integer, ClubInfo> clubMap;

    private static void init() {
        clubMap = new HashMap<Integer, ClubInfo>();
        clubMap.put(1, new ClubInfo(1, 999, new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)));
    }

    private static void bootServer() throws InterruptedException {
        init();
        NioEventLoopGroup parent = new NioEventLoopGroup(1);
        NioEventLoopGroup child = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap().group(parent, child).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_REUSEADDR, true).option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new ObjectDecoder(1024, ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                                    ctx.writeAndFlush("OK");
                                    super.channelRegistered(ctx);
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    System.err.println("异常，关闭连接");
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });

                            ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {

                                @Override
                                public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                    System.out.println("Server收到：" + msg);
                                    String[] ci = msg.split("/");
                                        ClubInfo club = clubMap.get(Integer.parseInt(ci[0]));
                                        if (ci[1].equals("null")) {
                                            if (!club.reachedMax()) {
                                                club.addChannelGroup(ctx.channel());
                                                System.out.println("连接加入channelGroup");
                                            } else ctx.writeAndFlush("已达到连接上限");
                                        } else club.writeGroupChannel(ci[1]);
                                }


                            });
                            System.out.println("新连接启动");
                        }

                    });
//            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            ChannelFuture f = b.bind(8888).sync();
            System.out.println("服务器启动");
            f.channel().closeFuture().sync();
        } finally {
            child.shutdownGracefully();
            parent.shutdownGracefully();
            System.out.println("服务器关闭");
        }
    }

    public static void main(String[] args) {
        try {
            bootServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
