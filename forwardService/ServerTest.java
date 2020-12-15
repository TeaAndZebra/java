package forwardService;

import dataPersistence.MysqlHandler;
import dataPersistence.RedisService;
import forwardService.handler.Handler;
import forwardService.utils.IdParser;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerTest {
    public static Handler handler;
    private static Logger logger = LogManager.getLogger(ServerTest.class.getName());
    private void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup(3);
        try {
/**
 * 给不同端口发送数据，但最终都是使用NioEventLoopGroup线程池里的线程
 *
 * */
            Bootstrap b0 = new Bootstrap();
            b0.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer< NioDatagramChannel >() {
                        @Override
                        public void initChannel(NioDatagramChannel ch) throws Exception {
                            ch.pipeline().addLast( new IdParser());
                        }});

            b0.bind(45685).sync().channel();






            Bootstrap b1 = new Bootstrap();
            b1.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer< NioDatagramChannel >() {
                        @Override
                        public void initChannel(NioDatagramChannel ch) throws Exception {
                            System.out.println(Thread.currentThread().toString());
                            ch.pipeline().addLast(handler=new Handler());
                        }});
            b1.bind(5467).sync().channel().closeFuture().await();
           // System.out.println(Thread.currentThread()+"  "+group.next()+"   "+group.next().next()+"   "+group.next().next().next()+"   "+group.next().next().next().next()+"     ");

        } finally {
            SharedTranMap.pool.destroy();
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
//        System.out.println("start");
        logger.info("start");
        /**
         * 核数有什么意义？？？
         * */
        ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
        /**
         * # 生成一个Jedis对象， 这个对象负责和指定Redis实例进行通信
         * 1.Jedis jedis = new Jedis("127.0.0.1", 6379)
         *2.Jedis(final String host, final int port, final int connectionTimeout, final int soTimeout)
         * 推荐
         * Jedis jedis = null;
         * try {
         * jedis = new Jedis("127.0.0.1", 6379);
         * jedis.get("hello");
         * } catch (Exception e) {
         * logger.error(e.getMessage(),e);
         * } finally {
         * if (jedis != null) {
         * jedis.close();
         * }
         * }
         * **/
        RedisService redisService = new RedisService();
        service.scheduleAtFixedRate(redisService, 0,15, TimeUnit.SECONDS);
        new MysqlHandler().setScheduler();
//        将redis Handler和monitor合并为RedisService

//        thread.start();
        new ServerTest().run();




    }
}
