package forwardService.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.sql.*;

public class DataBase {
   private Connection connection = null;
   private Statement statement = null;
   private String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
   private String DB_URL = "jdbc:mysql://39.97.171.14:3306/webrtclive?"
           +"user=root&password=123abc&useUnicode=true&characterEncoding=UTF-8";//&autoReconnect=true
    private Jedis jedis;
    private static Logger logger = LogManager.getLogger(DataBase.class.getName());
  public DataBase(){
       this.initial();
   }
   public Connection getConnection() {
       return connection;
   }

   private void initial(){
       try {
           Class.forName(JDBC_DRIVER);
       } catch (ClassNotFoundException e) {
//           e.printStackTrace();
           logger.error(e.getMessage(),e);
       }

       //  System.out.println("连接数据库...");
       //  System.out.println("实例化statement对象...");
       try {
           connection = DriverManager.getConnection(DB_URL);
           statement = connection.createStatement();
       } catch (SQLException e) {
//           e.printStackTrace();
           logger.error(e.getMessage(),e);
       }
   }
    public int getUserAdd(String user_id){

        int userAdd=0;
        try{
            String sql;

            sql = "SELECT user_add From user_ip where user_id='"+user_id+"'";//user_id:deviceID user_add:pdpAdd
            ResultSet rs = statement.executeQuery(sql);
            //  System.out.println(rs.toString());
            while ((rs.next())){
                // String user_id = rs.getString("user_id");
                userAdd = rs.getInt("user_add");
                logger.info("DataBase:getUserAdd:[{}]",userAdd);
            }

            rs.close();
           // System.out.println(connection.isValid(2));
          //  connection.close();

        }catch (SQLException se){
            // se.printStackTrace();
            logger.error(se.getMessage(),se);
        } catch (Exception e){
        // e.printStackTrace();
            logger.error(e.getMessage(),e);
        }
        return userAdd;

    }
    public String containPdpAdd(int pdpAddInt) {
        String IdString=null;
        ResultSet rs = null;
        ResultSet rsId = null;
        try {

            String sql;

            sql = "SELECT user_add From user_ip ";
            rs = statement.executeQuery(sql);
            //  System.out.println(rs.toString());
            while ((rs.next())) {
                // String user_id = rs.getString("user_id");
                int add = rs.getInt("user_add");
                if(add==pdpAddInt){
                 sql = "SELECT user_id FROM user_ip WHERE user_add='"+add+"'";
                  Statement  statement1 = connection.createStatement();
                 rsId = statement1.executeQuery(sql);
                 while (rsId.next()){
                     IdString = rsId.getString(1);
                 }

                 logger.debug("ID is:[{}]",IdString);

                }
            }




        } catch (SQLException se) {
        // se.printStackTrace();
            logger.error(se.getMessage(),se);
        } catch (Exception e) {
        //  e.printStackTrace();
            logger.error(e.getMessage(),e);
        }
        return IdString;
    }

}
