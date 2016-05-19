import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * Created by aojing on 2016/5/19.
 */
public class DJClient {

    private static ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectDecoder(1024, ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(new ObjectEncoder());
                            ch.pipeline().addLast(new DJReadHandler());
                            System.out.println("已连接服务器");
                        }

                    });
            ChannelFuture f = bootstrap.connect("127.0.0.1", 8888).sync();
            System.out.println("DJ客户端启动");
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
            System.out.println("DJ客户端关闭");
        }
    }

    private static class DJReadHandler extends SimpleChannelInboundHandler<String> {

        private final int[] colors = new int[11];

        DJReadHandler() {
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

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("DJ客户端收到消息：" + msg);
            pool.execute(new Runnable() {
                public void run() {
                    int i = 0;
                    while (true) {
                        if (i >= 11) i = 0;
                        String writeMsg = String.valueOf(1) + String.valueOf("/") + String.valueOf(colors[i++])
                                + String.valueOf(",") + String.valueOf(100);
                        ctx.writeAndFlush(writeMsg);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}
