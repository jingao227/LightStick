import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;

public class DJClient {
    private static int[] colors = new int[11];

    private static volatile ChannelGroup connectedChannel = null;

    private static int cur_color = 0xFF000000;

    private static long cur_tempo = 100;

    private static ExecutorService pool = Executors.newCachedThreadPool();

    private static void init() {
        colors[0] = 0xFF000000;
        colors[1] = 0xFF444444;
        colors[2] = 0xFF888888;
        colors[3] = 0xFFCCCCCC;
        colors[4] = 0xFFFFFFFF;
        colors[5] = 0xFFFF0000;
        colors[6] = 0xFF00FF00;
        colors[7] = 0xFF0000FF;
        colors[8] = 0xFFFFFF00;
        colors[9] = 0xFF00FFFF;
        colors[10] = 0xFFFF0000;
    }

    private static void sendDataPackage(int color, String pwd) {
        cur_color = color;
        String writeMsg = String.valueOf(1) + "/" + String.valueOf(pwd) + "/" +
                String.valueOf(cur_color) + "," + String.valueOf(cur_tempo);
        connectedChannel.writeAndFlush(writeMsg);
    }

    private static void sendDataPackage(long tempo, String pwd) {
        cur_tempo = tempo;
        String writeMsg = String.valueOf(1) + "/" + String.valueOf(pwd) + "/" +
                String.valueOf(cur_color) + "," + String.valueOf(cur_tempo);
        connectedChannel.writeAndFlush(writeMsg);
    }

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            init();
            connectedChannel = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectDecoder(1024, ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new DJReadHandler());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    connectedChannel = null;
                                    super.exceptionCaught(ctx, cause);
                                }
                            });
                            System.out.println("已连接服务器");
                        }

                    });
            ChannelFuture f = bootstrap.connect("172.21.14.64", 8888).sync();
            System.out.println("DJ客户端启动");

            new Thread(new Runnable() {
                int i = 0;

                public void run() {
                    while (connectedChannel != null) {
                        System.out.println("write");
                        if (i >= 11) i = 0;
                        sendDataPackage(colors[i++], "123");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

            f.channel().closeFuture().sync();

//            pool.execute(new Runnable() {
//                int i = 0;
//                public void run() {
//                    while (connectedChannel != null) {
//                        if (i >= 11) i = 0;
//                        sendDataPackage(colors[i++], "123");
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
            System.out.println("DJ客户端关闭");
        }
    }

    private static class DJReadHandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("DJ客户端收到消息：" + msg);
            connectedChannel.add(ctx.channel());
//            pool.execute(new Runnable() {
//                public void run() {
//                    int i = 0;
//                    while (true) {
//                        if (i >= 11) i = 0;
//                        String writeMsg = String.valueOf(1) + "/" + String.valueOf("123") + "/" +
//                                String.valueOf(colors[i++]) + "," + String.valueOf(100);
//                        ctx.writeAndFlush(writeMsg);
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
        }
    }
}
