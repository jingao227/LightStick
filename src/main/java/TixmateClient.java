import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;

/**
 * Created by aojing on 2016/5/19.
 */
public class TixmateClient {
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectDecoder(1024, ClassResolvers.cacheDisabled(null)));
                            ch.pipeline().addLast(new ReadHandler());
                            System.out.println("已连接服务器");
                        }

                    });
            ChannelFuture f = bootstrap.connect("172.21.14.64", 8888).sync();
            System.out.println("Tixmate客户端启动");
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
            System.out.println("Tixmate客户端关闭");
        }
    }

    private static class ReadHandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("Tixmate客户端收到消息：" + msg);
            if (msg.equals("OK")) ctx.writeAndFlush(String.valueOf(1) + String.valueOf("/") + String.valueOf("null"));
        }

    }
}
