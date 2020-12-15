package forwardService;

import entity.PdpSocket;
import entity.User;
import forwardService.handler.ServiceImpl;
import org.apache.commons.collections.map.MultiValueMap;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/* *
   * @Title: SharedTranMap
   * @Description: 提供服务器上所需的用户及信息映射关系
   * @param null:
   * @return
   * @Author: Wang Yueming
   * @Date: 2020/5/12
   */
public class SharedTranMap {

//    是否需要加volatile保证并发访问pdpPortMap安全？  volatile
//   仅仅解决了可见性的问题， 但是它并不能保证互斥性， 也就是说多个线程并发修改某个变量时， 依旧会产生多线程问题。 因此，不能靠volatile来完全替代传统的锁
//当一个变量被声明为volatile时，线程在写入变量时不会把值缓存在寄存器或者其他地方，而是会把值刷新到主内存。当其他线程读取该共享变量时 ，会从主内存重新获
//取最新值，而不是使用当前线程的工作内存中的值。
//  加锁本身就保证了内存可见性
    //final 修饰变量，说明变量为常量。如果为基本类型，则数据不能改变；若为引用类型，变量为引用变量的地址值，指向的变量不能变，但指向的变量的值可以改变
    public final static  MultiValueMap pdpPortMap = new MultiValueMap();/*(Int32位 pdpAdd)和byte pdpPort     线程不安全*/
    public static ConcurrentHashMap<PdpSocket, User> pdpSocketUserMap = new ConcurrentHashMap<>();//pdpSocket和User
    // public static ConcurrentHashMap<PdpSocket,Pdp> finalPdpSocketPdpMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<User, ServiceImpl> regImplWithObject = new ConcurrentHashMap<>();//regImpl和User
    private static CopyOnWriteArrayList<User> arrayList = new CopyOnWriteArrayList<>();

    public static JedisPool pool = new JedisPool(new JedisPoolConfig(),"localhost");
    /**
     * 2020/6/1 避免并发访问pdpPortMap
     * 只要是对pdpPortMap的操作都需要被锁
     *  synchronized 块的内存语义是把在 synchronized 块内使用到的变量从线程的工作内存
     * 中清除，这样在 synchronized 块 内使用到该变量时就不会从线程的工作内存中获取，而是
     * 直接从主内存中获取 。退出 synchronized 块的内存语义是把在 synchronized 块内对共享变
     * 量的修改刷新到主内存 。*/
//    public static void modPdpPortMap(String type,int pdpAddInt, byte pdpPort){
//        synchronized (pdpPortMap){
//            if(type.equals("put")){
//                pdpPortMap.put(pdpAddInt,pdpPort);
//            }else if(type.equals("remove")){
//                pdpPortMap.remove(pdpAddInt,pdpPort);
//            }
//        }
//    }


/* *
   * @Title: getUserList
   * @Description: 获得当前所有的用户
   * @param :
   * @return java.util.ArrayList<entity.User>
   * @Author: Wang Yueming
   * @Date: 2020/5/12
   */
public static CopyOnWriteArrayList<User> getUserList(){
    if(pdpSocketUserMap ==null|| pdpSocketUserMap.size()==0){
        return null;
    }
    try {
        for(Map.Entry<PdpSocket,User> entry: pdpSocketUserMap.entrySet()){
//        System.out.println(entry+" ");
            arrayList.add(entry.getValue());

        }
    }catch (Exception e){
        e.printStackTrace();
    }
    return arrayList;
}


}
