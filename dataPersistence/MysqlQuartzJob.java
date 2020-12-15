package dataPersistence;

import entity.PdpSocket;
import entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import forwardService.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * 每15s在redis数据库中存入当前数据流量
 * dataPersistence.MysqlQuartzJob
 * 每天0点在mysql数据库中插入item（一天的流量），并清空redis数据库流量
 * **/
public class MysqlQuartzJob implements Job {
    private  Connection connection = null;
    private  Statement statement = null;
    private  String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private  String DB_URL = "jdbc:mysql://39.97.171.14:3306/webrtclive?"
            +"user=root&password=123abc&useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT";//&autoReconnect=true
    static Logger logger = LogManager.getLogger(MysqlQuartzJob.class.getName());


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("insert into mysql");
        /**connect to mysql database*/
        Jedis jedis = new Jedis("localhost");
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        try {
            connection = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
//                    e.printStackTrace();
            logger.error(e.getMessage(), e);
        }

        //  System.out.println("实例化statement对象...");
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }

        ArrayList<String> users = new ArrayList<>();
        users.addAll(jedis.keys("u:info:*"));


        for(String key:users){
            String[] splits = key.split(":");
            String userRedisKey = splits[2]+":"+splits[3];
//            System.out.println(userRedisKey);

            Float flow = Float.valueOf(jedis.hget(key, "flow"));
            logger.debug("[{}] previous user flow is [{}]", userRedisKey,flow);
            if (flow != null && flow != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
                sdf.applyPattern("yyyy-MM-dd HH:mm:ss");//
                Date date = new Date();// 获取当前时间
                String time = sdf.format(date);
                logger.debug("time is [{}]",time);
//                    System.out.println("time is : " + time+" now"); // 输出已经格式化的现在时间（24小时制）
                try {
                    String sql = "INSERT INTO user_daily_flow(user,flow,time) " +
                            "VALUES ('" + userRedisKey + "','" + flow + "','" + time + "')";
                    statement.execute(sql);
                    /**redis 清0*/
                    try {
                        jedis.del(key);

                    }catch (JedisConnectionException e){
                        logger.error(e.getMessage(), e);
                    }

                } catch (SQLException se) {
//                            se.printStackTrace();
                    logger.error(se.getMessage(), se);
                } catch (Exception e) {
//                           e.printStackTrace();
                    logger.error(e.getMessage(), e);
                }
            }
        }

        try {
            connection.close();
            jedis.close();
        } catch (SQLException e) {
//                    e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }




}
