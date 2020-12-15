package forwardService.handler;

import entity.PdpSocket;
import entity.User;
import forwardService.utils.DataBase;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import forwardService.*;
import forwardService.utils.DataChange;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Handler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(),cause);
    }
    private static DataBase dataBase;
    public Handler(){
    }
    private static Logger logger = LogManager.getLogger(Handler.class.getName());
    private static ServiceImpl service = new ServiceImpl();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.info("[{}] Handler channel active",Thread.currentThread().getName());
            dataBase = new DataBase();

    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        msg.retain();
        ByteBuf buf =msg.content();
       try {
           byte destNum = buf.getByte(0);//有符号整形的目的地址数
            /*
            当首字节大于等于0时，第一次服务器对源地址自动注册，
           并且之后每收到一次来自该源地址的报文就更新一次对应的IP地址；
           当首字节小于0时，服务器对该源地址注销。
            */
           byte[] pdpAddByte = new byte[4];
           buf.getBytes(1,pdpAddByte,0,4);
           int pdpAddInt =  DataChange.bytes2Int(pdpAddByte);
           byte pdpPort =  buf.getByte(5);
           //为了不创造多余对象，即收到一个数据报就创建一个ServiceImpl对象。我们 将一个用户对应的ServiceImpl对象存起来
           PdpSocket pdpSocket = new PdpSocket(pdpAddInt, pdpPort);//源地址
           if(destNum>=0){//首字节大于等于0，第一次注册，之后转发数据。每次更新一次ip地址
               if(!SharedTranMap.pdpSocketUserMap.containsKey(pdpSocket)) {
                   // System.out.println("des="+destNum+":转发");
                   service.register(ctx, dataBase, msg);
                   //System.out.println("des="+destNum+"注册");
               }
             //  logger.debug("desNum = [{}]",destNum);
               User user = SharedTranMap.pdpSocketUserMap.get(pdpSocket);
               ServiceImpl reg = SharedTranMap.regImplWithObject.get(user);
               service.updateIp(ctx, msg,pdpSocket);//先更新IP再转发
               reg.multiRoute(ctx, msg, user);

           }else{//首字节小于0，注销
               service.cancel(ctx, msg);
           }
       }catch (Exception e){
           e.printStackTrace();
           logger.info("received bytes wrong :[{}]",msg);
       }

    }
}
