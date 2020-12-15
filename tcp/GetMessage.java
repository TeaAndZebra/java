package tcp;


import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetMessage {

    public static final String ADD_URL = "http://www.shanontech.com//devCheck";


    public String getMsg(String id) throws IOException {
        HttpURLConnection connection = null;
        try {
            //创建连接
            StringBuffer echo=null;
            URL url = new URL(ADD_URL);
            connection = (HttpURLConnection) url.openConnection();


            //设置http连接属性
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST"); // 可以根据需要 提交 GET、POST、DELETE、INPUT等http提供的功能
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);


            //设置http头 消息
            connection.setRequestProperty("Content-Type", "application/json");  //设定 请求格式 json，也可以设定xml格式的
            //connection.setRequestProperty("Content-Type", "text/xml");   //设定 请求格式 xml，
            connection.setRequestProperty("Accept", "application/json");//设定响应的信息的格式为 json，也可以设定xml格式的
//             connection.setRequestProperty("X-Auth-Token","xx");  //特定http服务器需要的信息，根据服务器所需要求添加
            connection.connect();

            //添加 请求内容
            JSONObject user = new JSONObject();
            user.put("devid",id);

            //构建嵌套的 json数据
            OutputStream out = connection.getOutputStream();
            out.write(user.toString().getBytes());
            out.flush();
            out.close();

//            读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            StringBuffer sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                sb.append(lines);
            }
           // System.out.println(sb);

            reader.close();
//          断开连接

            connection.disconnect();
            JSONObject json = JSONObject.parseObject(sb.toString());
            if(json.get("state").equals("success")){

//            返回给终端设备的信息
                echo = new StringBuffer();
                echo.append("ID:");
                echo.append(id);

                echo.append("\nEncoder:");
                echo.append(json.getString("Encoder"));

                echo.append("\nCodeRate:");
                echo.append(json.getString("CodeRate"));

                echo.append("\nFrameRate:");
                echo.append(json.getString("FrameRate"));

                echo.append("\nResolution:");
                echo.append(json.getString("Resolution"));


                String linkStr = json.getString("links");

                //添加音频连接信息
                for(int channel=0;channel<16;channel++){
                    String audioMap = JSONObject.parseObject(linkStr).getString("audio");
                    String audios = JSONObject.parseObject(audioMap).getString("LinkAudio"+channel);
                    if( audios !=null && audios.length()>0){
                        StringBuffer linkReturn = new StringBuffer();
                        String regEx="[\\[\\],\"]";
                        String[] sss = audios.split(regEx);
                        for(String str:sss){
                            linkReturn.append(str);
                            if(!str.equals(""))
                                linkReturn.append(",");
                        }
                        echo.append("\nLinkAudio"+channel);
                        echo.append(":"+linkReturn.toString());
                    }
                }
                //添加视频连接信息
                for(int channel=0;channel<16;channel++){
                    String videoMap = JSONObject.parseObject(linkStr).getString("video");
                    String videos = JSONObject.parseObject(videoMap).getString("LinkVideo"+channel);
                    if( videos !=null && videos.length()>0){
                        StringBuffer linkReturn = new StringBuffer();
                        String regEx="[\\[\\],\"]";
                        String[] sss = videos.split(regEx);
                        for(String str:sss){
                            linkReturn.append(str);
                            if(!str.equals(""))
                                linkReturn.append(",");
                        }
                        echo.append("\nLinkVideo"+channel);
                        echo.append(":"+linkReturn.toString());
                    }
                }
                //添加串口连接信息
                for(int channel=0;channel<16;channel++){
                    String boxMap = JSONObject.parseObject(linkStr).getString("box");
                    String boxes = JSONObject.parseObject(boxMap).getString("LinkBox"+channel);
                    if( boxes !=null && boxes.length()>0){
                        StringBuffer linkReturn = new StringBuffer();
                        String regEx="[\\[\\],\"]";
                        String[] sss = boxes.split(regEx);
                        for(String str:sss){
                            linkReturn.append(str);
                            if(!str.equals(""))
                                linkReturn.append(",");
                        }
                        echo.append("\nLinkBox"+channel);
                        echo.append(":"+linkReturn.toString());
                    }
                }
                //添加信令连接信息
                for(int channel=0;channel<16;channel++){
                    String cmdMap = JSONObject.parseObject(linkStr).getString("cmd");
                    String cmds = JSONObject.parseObject(cmdMap).getString("LinkCmd"+channel);
                    if( cmds !=null && cmds.length()>0){
                        StringBuffer linkReturn = new StringBuffer();
                        String regEx="[\\[\\],\"]";
                        String[] sss = cmds.split(regEx);
                        for(String str:sss){
                            linkReturn.append(str);
                            if(!str.equals(""))
                                linkReturn.append(",");
                        }
                        echo.append("\nLinkCmd"+channel);
                        echo.append(":"+linkReturn.toString());
                    }
                }

                return echo.toString();

            }

          //  System.out.println(json.get("state"));
          //  System.out.println(json.get("links"));

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
       } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


}