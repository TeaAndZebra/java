package forwardService.handler;

import entity.PdpSocket;
import entity.User;
import forwardService.*;
import forwardService.utils.DataBase;
import forwardService.utils.DataChange;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServiceImpl implements Service {
    private static Logger logger = LogManager.getLogger(ServiceImpl.class.getName());
    private int TIME_OUT = 3;
    private int TIME_CALSPEED = 10;
//    private ScheduledExecutorService myService = new ScheduledThreadPoolExecutor(3);
    private PdpSocket pdpSocket;
    private ServiceImpl(PdpSocket pdpSocket){

        this.pdpSocket = pdpSocket;
    }
    ServiceImpl(){

    };

    /**
     *
     * 注册地址：
     * 用户发送	目的地址数(8bits >=0) PDP地址（32bit）	PDP端口（8bit） 数据
     * 服务器返回0xD6	0x03	错误码（8bit）	IP端口（16bit）
     */
    @Override
    public void register(ChannelHandlerContext ctx, DataBase dataBase,DatagramPacket msg) throws Exception {
        msg.retain();//引用计数类型
        ByteBuf buf = msg.content();
        logger.debug("apply for register");
        byte[] pdpAddByte = new byte[4];
        buf.getBytes(1, pdpAddByte, 0, 4);
        byte pdpPort = buf.getByte(5);//PDP端口号
        int pdpAddInt = DataChange.bytes2Int(pdpAddByte);
        // System.out.println(pdpAddInt + "apply for register");
        /**新建对象*/
        User user;
        PdpSocket pdpSocket = new PdpSocket(pdpAddInt, pdpPort);
        /**
         * 由pdpSocketPdpMap是否存在对应pdpSocket判断
        if (SharedTranMap.pdpSocketUserMap.containsKey(pdpSocket)) {
            logger.info("[{}] repeat register", pdpAddInt);
            user = SharedTranMap.pdpSocketUserMap.get(pdpSocket);
            // System.out.println("分配ip端口为：" + echoPort);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            user.setLogInTime(dateFormat.format(new Date()) + "repeat register");
            user.setLogOffTime("");
            user.setCtx(ctx);
            user.setIpAdd(msg.sender());
            user.getTimer().cancel(false);//取消上次计时

            ScheduledFuture future = ctx.channel().eventLoop().schedule(new Remover(user), 30, TimeUnit.SECONDS);
            user.setTimer(future);//开始新的计时
        } else {
        */
            String IdString;
            /** System.out.println("初次注册");*/
            if (!dataBase.getConnection().isValid(2)) {
                dataBase = new DataBase();
            }
            if ((IdString = dataBase.containPdpAdd(pdpAddInt))!=null) {
                user = new User(pdpSocket);
                logger.debug("[{}] first register success", user.toString());
                user.setIDString(IdString);
                /**2020/6/1 避免并发对pdpPortMap操作
                 *
                 * 将 pdpAddInt及对应pdpPort存入map*/
                SharedTranMap.pdpPortMap.put( pdpAddInt, pdpPort);
                /**存入对象及其socket(Socket,Object)*/
                SharedTranMap.pdpSocketUserMap.put(pdpSocket, user);

                ServiceImpl reg = new ServiceImpl(pdpSocket);

                /**配置pdp属性参数*/
                SharedTranMap.regImplWithObject.put(user, reg);
                user.setCtx(ctx);
                user.setIpAdd(msg.sender());

                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                user.setLogInTime(dateFormat.format(date) + "first register");
                user.setLogOffTime("");
                ScheduledFuture future = ctx.channel().eventLoop().scheduleAtFixedRate(new Remover(user), 0,TIME_OUT,TimeUnit.SECONDS);
//                (new Remover(user), 30, TimeUnit.SECONDS);

                user.setTimer(future);
                ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
                //计算用户发送数据的速度
                ScheduledFuture calSpeedFuture = service.scheduleAtFixedRate(new Runnable() {   //会不会产生并发问题
                    @Override
                    public void run() {
                        //                   if(user!=null) {
                        user.setSpeedOfDatagram(user.getTestOfSpeed() / TIME_CALSPEED);
                        user.setTestOfSpeed(0);
                        //                      }
                    }
                }, 0, TIME_CALSPEED, TimeUnit.SECONDS);
                user.setCalSpeedFuture(calSpeedFuture);

            } else {
                /**错误码*/
//                System.out.println(pdpAddInt+" first register error");
                logger.debug("invalid user [{}] in first register ", pdpAddInt);

            }
//        }

    }




    /**
     * 更新IP地址
     * 用户发送	目的地址数(8bits >=0) PDP地址（32bit）	PDP端口（8bit） 数据
     * 接收到用户发送的数据，立即更新IP地址
     */
    @Override
    public void updateIp(ChannelHandlerContext ctx, DatagramPacket msg,PdpSocket pdpSocket)  {
        //logger.debug("update ip");
        if (SharedTranMap.pdpSocketUserMap.containsKey(pdpSocket)) {
            //System.out.println(pdpAddInt + "update IP");
            User user = SharedTranMap.pdpSocketUserMap.get(pdpSocket);
           // logger.debug("[{}] update ip", user.toString());

            user.setIpAdd(msg.sender());
            user.setCtx(ctx);
            //重启定时器
            user.getTimer().cancel(false);
            //   ctx.executor().shutdownGracefully();
            ScheduledFuture future =  ctx.channel().eventLoop().schedule(new Remover(user), TIME_OUT, TimeUnit.SECONDS);
            user.setTimer(future);
        } else {
            logger.debug("address error when updating ip");
        }
    }



    /**
     * 注销
     * 用户发送	目的地址数(8bits<0) PDP地址（32bit）	PDP端口（8bit） 数据
     */
    @Override
    public void cancel(ChannelHandlerContext ctx,DatagramPacket msg)  {
        msg.retain();//引用计数类型
        ByteBuf buf = msg.content();
        logger.info("cancel");
        byte[] pdpAddByte = new byte[4];
        buf.getBytes(1, pdpAddByte, 0, 4);

        int pdpAddInt = DataChange.bytes2Int(pdpAddByte);
        byte pdpPort = buf.getByte(5);
        PdpSocket pdpSocket = new PdpSocket(pdpAddInt, pdpPort);
        /**
         * 注销在pdpSocketPdpMap及pdpPortMap及regImplWithObject中进行删除*/
        if (SharedTranMap.pdpSocketUserMap.containsKey(pdpSocket)) {
//            System.out.println(pdpAddInt + "cancel");
            User user = SharedTranMap.pdpSocketUserMap.get(pdpSocket);
            logger.info("[{}] cancel", user.toString());
            Remover remover = new Remover(user);
            ctx.channel().eventLoop().execute(remover);
//            remover.removeSetRedis();
//            remover.removeFromCache();

            /*当客户登出时，redis中的缓存应该设置好登出时间，再将user从当前用户列表中删除*/


        }
    }

    /**多播路由：
     *用户发送目的地址数（8bit>0） 源地址（40bit）		目的地址0（40bit）	目的地址……（40bit）用户数据*/
    @Override
    public void multiRoute(ChannelHandlerContext ctx, DatagramPacket msg, User user) throws Exception {
        msg.retain();
        ByteBuf buf =msg.content();
        int rBytesOfBuf= buf.readableBytes();
        Handler handler =ServerTest.handler;
        if(handler !=null&& user !=null) {
            //  System.out.println(user.getPdpSocket() + " multiCast");
            //logger.debug("multiCast bits of data is [{}]",rBytesOfBuf);
            byte numOfDesAdd = buf.getByte(0);
            // System.out.println("multicast:  num is:" + numOfDesAdd);


            /*计算每个pdp地址对应的数据量*/
            user.setBitsOfDatagram(user.getBitsOfDatagram() + rBytesOfBuf);
            user.setTestOfSpeed(user.getTestOfSpeed() + rBytesOfBuf);
         //   logger.debug("receive [{}] bytes ,now testOfByte is [{}]",rBytesOfBuf,user.getTestOfSpeed());

            /*计算每个pdp地址对应的数据包数*/
            user.setNumOfDatagram(user.getNumOfDatagram() + 1);

            /*添加*/
            for (int i = 0; i < numOfDesAdd; i++) {
                byte[] dest = new byte[4];
                buf.getBytes(6 + i * 5, dest, 0, 4);
                byte destPort = buf.getByte(10 + 5 * i);
                int pdpSocketIntDes = DataChange.bytes2Int(dest);
                 //logger.info("multicast: "+" pdpAdd:"+DataChange.bytes2Int(dest)+"port:"+destPort);
                PdpSocket pdpSocketDes = new PdpSocket(pdpSocketIntDes, destPort);
                if (SharedTranMap.pdpSocketUserMap.containsKey(pdpSocketDes)) {
                    // System.out.println(user.getPdpSocket()+  " multiCast desAdd is "+pdpSocketIntDes+" port is"+destPort);
                    logger.info(" multiCast [{}] send packages to [{}]",pdpSocket.toString(),pdpSocketDes.toString());
                    User userDest = SharedTranMap.pdpSocketUserMap.get(pdpSocketDes);
                    userDest.getCtx().writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(buf), userDest.getIpAdd()));
                    break;
                }else{
                    logger.info("multiCast dest pdpAdd:[{}] error ",pdpSocketDes);
                }
            }
        }else {
            logger.info("multiCast fail : handler=null || user:{[{}]}=null ", user);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceImpl other = (ServiceImpl)obj;
        if (pdpSocket!=other.pdpSocket)
            return false;
        return true;

    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pdpSocket.hashCode();
        return result;
    }


}
