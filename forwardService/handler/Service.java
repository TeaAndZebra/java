package forwardService.handler;

import forwardService.utils.DataBase;
import entity.PdpSocket;
import entity.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

public interface Service {
    void register(ChannelHandlerContext ctx, DataBase dataBase, DatagramPacket msg) throws Exception;
    void updateIp(ChannelHandlerContext ctx, DatagramPacket msg, PdpSocket pdpSocket)throws Exception;
    void cancel(ChannelHandlerContext ctx,DatagramPacket msg);
    void multiRoute(ChannelHandlerContext ctx, DatagramPacket msg, User user) throws Exception;


}
