package tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TcpGetInfo {
    private static Logger logger = LogManager.getLogger(TcpGetInfo.class.getName());
    private void startTcp(){
       EventLoopGroup acceptor = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
         try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(acceptor,worker);
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new GetInfoHandler());
                }
            });
            ChannelFuture f = bootstrap.bind(45684).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
//            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } finally {
            acceptor.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        logger.info("TcpGetInfo start");
        new TcpGetInfo().startTcp();
    }
}
