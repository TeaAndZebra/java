package tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetInfoHandler extends ChannelHandlerAdapter {
    private static Logger logger = LogManager.getLogger(GetInfoHandler.class.getName());
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      //  System.out.println("连上");

        ByteBuf instr = (ByteBuf)msg;
        byte[] str = new byte[instr.readableBytes()];
        instr.getBytes(0,str,0,instr.readableBytes());
        String string = new String(str);
        String echo;
        if(instr.getByte(12)=='\n'){
           String[] split = string.split("\\n");
           if(split[0].equals("GET ID TABLE")){
//               System.out.println(split[0]+"---"+split[1]);
               logger.debug("[{}] --- [{}]",split[0],split[1]);
               String id = split[1];
               echo = new GetMessage().getMsg(id);
               if(echo!=null){
                   ByteBuf buf = Unpooled.buffer();
                   buf.writeBytes(echo.getBytes());
                  // System.out.println(echo);
                   ctx.writeAndFlush(buf);
               }
               ctx.close();
           }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
