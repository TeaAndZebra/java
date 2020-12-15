package entity;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledFuture;

public class User implements Serializable {
    /*
    在一个线程set，其余线程get则保证可见性。
    多个线程set，则需要加锁
    * */
    private volatile PdpSocket pdpSocket;
    private volatile long bitsOfDatagram; //接收到的bit数
    private volatile long numOfDatagram;  //接收到的包数
    private volatile long testOfSpeed;
    private volatile long speedOfDatagram;
    private volatile ChannelHandlerContext ctx;
    private volatile InetSocketAddress ipAdd;
    private volatile String logInTime;
    private volatile String logOffTime;

    private String IDString;

    public String getIDString() {
        return IDString;
    }

    public void setIDString(String IDString) {
        this.IDString = IDString;
    }

    private volatile ScheduledFuture calSpeedFuture;//计算用户发送数据速率
    private volatile ScheduledFuture timer;
    private volatile Double currentFlow;
    //
    public User(PdpSocket pdpSocket){
        this.pdpSocket = pdpSocket;
    }
    @Override
    public String toString() {
        return pdpSocket.toString();
    }

    public Double getCurrentFlow() {
        return currentFlow;
    }

    public void setCurrentFlow(Double currentFlow) {
        this.currentFlow = currentFlow;
    }

    public ScheduledFuture getTimer() {//用于重启定时器
        return timer;
    }

    public void setTimer(ScheduledFuture timer) {
        this.timer = timer;
    }//设置定时器

    public ScheduledFuture getCalSpeedFuture() {////计算用户发送数据速率的future
        return calSpeedFuture;
    }

    public void setCalSpeedFuture(ScheduledFuture calSpeedFuture) {
        this.calSpeedFuture = calSpeedFuture;
    }

//    public boolean isActive() {
//        return isActive;
//    }
//
//    public void setActive(boolean active) {
//        isActive = active;
//    }

    public String getLogOffTime() {
        return logOffTime;
    }

    public synchronized void setLogOffTime(String logOffTime) {
        this.logOffTime = logOffTime;
    }


    public PdpSocket getPdpSocket() {
        return pdpSocket;
    }

    public  long getBitsOfDatagram() {
        return this.bitsOfDatagram;
    }

    public  void setBitsOfDatagram(long bitsOfDatagram) {//synchronized  锁住的是对象：非静态方法锁住的是this对象
        this.bitsOfDatagram = bitsOfDatagram;
    }

    public long getNumOfDatagram() {
        return numOfDatagram;
    }

    public void setNumOfDatagram(long numOfDatagram) {
        this.numOfDatagram = numOfDatagram;
    }

    public long getTestOfSpeed() {
        return testOfSpeed;
    }

    public synchronized void setTestOfSpeed(long testOfSpeed) {
        this.testOfSpeed = testOfSpeed;
    }

    public long getSpeedOfDatagram() {
        return speedOfDatagram;
    }

    public void setSpeedOfDatagram(long speedOfDatagram) {
        this.speedOfDatagram = speedOfDatagram;
    }


    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public InetSocketAddress getIpAdd() {
        return ipAdd;
    }

    public void setIpAdd(InetSocketAddress ipAdd) {
        this.ipAdd = ipAdd;
    }

    public String getLogInTime() {
        return logInTime;
    }

    public void setLogInTime(String logInTime) {
        this.logInTime = logInTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (!pdpSocket.equals(other.pdpSocket) )
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
