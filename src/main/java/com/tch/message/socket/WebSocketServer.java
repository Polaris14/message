package com.tch.message.socket;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tch.message.mapper.GroupMapper;
import com.tch.message.mapper.UserMapper;
import com.tch.message.model.Group;
import com.tch.message.model.Message;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{userId}")
public class WebSocketServer {

    //声明ApplicationContext
    private static ApplicationContext applicationContext;

    //注入applicationContext
    public static void setApplicationContext(ApplicationContext applicationContext){
        WebSocketServer.applicationContext = applicationContext;
    }

    //记录容器中的数量
    private static int count = 0;

    //创建一个存放用户的容器
    private static ConcurrentHashMap<Integer,WebSocketServer> socket = new ConcurrentHashMap<Integer,WebSocketServer>();

    //组装发送数据的格式
    private static HashMap<String,Object> hashMap = new HashMap<>();

    //声明客户端的连接会话，用于给客户端发送数据
    private Session session;

    //声明一个用户id
    private Integer userId;

    //线程安全的统计用户数量
    public static synchronized int count(){
        return count;
    }

    public static synchronized void addCount(){
        WebSocketServer.count++;
    }

    public static synchronized void subCount(){
        WebSocketServer.count--;
    }

    //连接建立成功调用的方法
    @OnOpen
    public void onOpen(@PathParam("userId") Integer userId, Session session){
        this.userId = userId;
        this.session = session;
        socket.put(userId,this);
        addCount();
        System.out.println("用户：" + userId + "连接成功---" + count);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (!userId.equals("")) {
            socket.remove(userId);  //根据用户id从ma中删除
            subCount();           //在线数减1
            System.out.println("断开连接");
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        Message parse = JSONObject.parseObject(message, Message.class);
        if("friend".equals(parse.getData().getTo().getType())){
            try{
                socket.get(parse.getData().getTo().getId()).sendFriendMessage(parse);
            }catch (Exception e){
                session.getBasicRemote().sendText("0");
            }
        }else{
            sendGroupMessage(parse);
        }
    }

    //群发
    private void sendGroupMessage(Message parse) throws IOException {
        GroupMapper groupMapper = applicationContext.getBean(GroupMapper.class);
        Group group = groupMapper.findGroupById(parse.getData().getTo().getId());
        List<Integer> ids = JSONArray.parseArray(group.getUids(), Integer.class);
        for (Integer id:ids) {
            if(id == userId){
                continue;
            }
            if(socket.get(id) != null){
                hashMap.put("emit",parse.getType());
                Map map = toolMap(parse);
                map.put("id",parse.getData().getTo().getId());
                hashMap.put("data",map);
                socket.get(id).session.getBasicRemote().sendText(JSONObject.toJSONString(hashMap));
            }
        }
    }

    //单聊
    private void sendFriendMessage(Message parse) throws IOException {
        hashMap.put("emit",parse.getType());
        Map map = toolMap(parse);
        map.put("id",parse.getData().getMine().getId());
        hashMap.put("data",map);
        this.session.getBasicRemote().sendText(JSONObject.toJSONString(hashMap));
    }

    private Map toolMap(Message parse){
        HashMap<String, Object> map = new HashMap<>();
        map.put("username",parse.getData().getMine().getUsername());
        map.put("avatar",parse.getData().getMine().getAvatar());
        map.put("id",parse.getData().getMine().getId());
        map.put("type",parse.getData().getTo().getType());
        map.put("content",parse.getData().getMine().getContent());
        map.put("cid",new Date().getTime());
        map.put("mine",false);
        map.put("fromid",parse.getData().getMine().getId());
        map.put("timestamp",new Date().getTime());
        return map;
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
}
