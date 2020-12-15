package forwardService.utils;

import dataPersistence.RedisService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

public class IdParser extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger logger = LogManager.getLogger(IdParser.class);

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        DataBase dataBase = new DataBase();
        parseId(ctx, dataBase, msg);
    }

    /**
     * ID解析:
     * 用户发送 字符串长度(32bit) |  字符串数据
     * 服务器返回0xD6	0x01	错误码（8bit）	PDP地址（32bit）
     */

    public void parseId(ChannelHandlerContext ctx, DataBase dataBase, DatagramPacket msg) throws Exception {
        msg.retain();//引用计数类型
        ByteBuf buf = msg.content();
        logger.debug("parse id");
        byte[] lengthOfIdB = new byte[4];
        buf.getBytes(0,lengthOfIdB, 0, 4);
        int lengthOfId = DataChange.bytes2Int(lengthOfIdB);
        byte[] ID = new byte[lengthOfId];
        buf.getBytes(4, ID, 0, lengthOfId);
        String IDString = new String(ID, "US-ASCII");//deviceID
        // System.out.println(IDString + "id translate");
        byte[] echo = new byte[4];

        int pdpAddInt;
        /** 从数据库取出对应pdpAdd*/
        if (dataBase.getConnection().isValid(2)) {
            pdpAddInt = dataBase.getUserAdd(IDString);//pdpAdd
        } else {
            dataBase = new DataBase();
            pdpAddInt = dataBase.getUserAdd(IDString);
        }
        if (pdpAddInt != 0) {
            byte[] pdpAddByte = DataChange.IntToBytes(pdpAddInt);
            System.arraycopy(pdpAddByte, 0, echo, 0, 4);
        }

        /**从数据库查询ID对应PDP地址*/
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(echo), msg.sender()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
