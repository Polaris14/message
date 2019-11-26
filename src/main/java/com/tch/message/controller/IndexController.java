package com.tch.message.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tch.message.mapper.FriendMapper;
import com.tch.message.mapper.GroupMapper;
import com.tch.message.mapper.GroupNameMapper;
import com.tch.message.mapper.UserMapper;
import com.tch.message.model.Friend;
import com.tch.message.model.Group;
import com.tch.message.model.User;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
public class IndexController {

    @Resource
    private UserMapper userMapper;

    @Resource
    private FriendMapper friendMapper;

    @Resource
    private GroupNameMapper groupNameMapper;

    @Resource
    private GroupMapper groupMapper;

    @Value("${file.path}")
    private String root;

    @GetMapping("/index/{userId}")
    public String test1(@PathVariable("userId")Integer userId, Model model){
        //当前用户信息
        User user = userMapper.findUserById(userId);
        Map<String, Object> userMap = toolMap(user);
        model.addAttribute("mine",JSONObject.toJSONString(userMap));

        //查询单聊好友列表
        List<Friend> friends = friendMapper.findFriendByUserID(user.getId());
        HashSet<Map> sets = new HashSet<>();
        //根据分组查询好友
        for (Friend friend:friends) {
            HashMap<String, Object> map = new HashMap<>();
            //查询单聊分组
            String groupName = groupNameMapper.findGroupNameById(friend.getGroupName());
            map.put("groupname",groupName);
            map.put("id",friend.getId());
            //字符串id转集合
            List<Integer> integers = JSONObject.parseArray(friend.getFid(), Integer.class);
            HashSet<Map> users = new HashSet<>();
            for (Integer id:integers) {
                User user1 = userMapper.findUserById(id);
                Map<String, Object> stringObjectMap = toolMap(user1);
                users.add(stringObjectMap);
            }
            map.put("list",users);
            sets.add(map);
        }
        model.addAttribute("friend",JSONObject.toJSONString(sets));

        //查询群聊
        List<Integer> gids = JSONArray.parseArray(user.getGids(), Integer.class);
        HashSet<Map> set = new HashSet<>();
        for (Integer gid:gids) {
            Group group = groupMapper.findGroupById(gid);
            String groupName = groupNameMapper.findGroupNameById(group.getGroupName());
            HashMap<String, Object> map = new HashMap<>();
            map.put("groupname",groupName);
            map.put("id",group.getId());
            map.put("avatar",group.getAvatar());
            set.add(map);
        }
        model.addAttribute("group",JSONObject.toJSONString(set));
        return "index";
    }

    //数据组装
    private Map<String,Object> toolMap(User user){
        HashMap<String, Object> map = new HashMap<>();
        map.put("username",user.getUsername());
        map.put("id",user.getId());
        map.put("status",user.getOnline());
        map.put("sign",user.getSign());
        map.put("avatar",user.getAvatar());
        return map;
    }


    @PostMapping("upload/image")
    @ResponseBody
    public Map image(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        String split = filename.split("\\.")[1];
        String uuid = String.valueOf(UUID.randomUUID());
        File file1 = new File(root + File.separator + uuid + "." + split);
        file.transferTo(file1);
        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("src","http://47.103.216.39/1q2w3etch/" + uuid + "." + split);
        map.put("code","0");
        map.put("msg","");
        map.put("data",hashMap);
        return map;
    }

    @PostMapping("/upload/file")
    @ResponseBody
    @ExceptionHandler(FileUploadBase.SizeLimitExceededException.class)
    public Map file(MultipartFile file) throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, String> hashMap = new HashMap<>();
        if(file.getSize() > (30 * 1024 *1024)){
            hashMap.put("src","");
            hashMap.put("name","");
            map.put("code","1");
            map.put("msg","文件最大上传30MB");
            map.put("data",hashMap);
            return map;
        }
        String filename = file.getOriginalFilename();
        String split = filename.split("\\.")[1];
        String uuid = String.valueOf(UUID.randomUUID());
        File file1 = new File(root + File.separator + uuid + "." + split);
        file.transferTo(file1);
        hashMap.put("src","http://47.103.216.39/1q2w3etch/" + uuid + "." + split);
        hashMap.put("name",filename);
        map.put("code","0");
        map.put("msg","");
        map.put("data",hashMap);
        return map;
    }

    @GetMapping("/getMembers")
    @ResponseBody
    public Map getMembers(@RequestParam("id") Integer id){
        Group group = groupMapper.findGroupById(id);
        String uids = group.getUids();
        List<Integer> list = JSONArray.parseArray(uids, Integer.class);
        ArrayList<User> users = new ArrayList<>();
        for (Integer uid:list) {
            users.add(userMapper.findUserById(uid));
        }
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("list",users);
        HashMap<String, Object> map = new HashMap<>();
        map.put("code","0");
        map.put("msg","");
        map.put("data",hashMap);
        return map;
    }

    @GetMapping("/changSign/{value}/{id}")
    public String changSign(@PathVariable("value") String sign,@PathVariable("id") Integer id){
        int i = userMapper.updateSignById(sign,id);
        return "index";
    }

    @PostMapping("login")
    @ResponseBody
    public Map login(@RequestBody User user, HttpSession session){
        HashMap<String, Object> map = new HashMap<>();
        map.put("code",0);
        User u = userMapper.findUserByUserNameAndPassword(user.getUsername(),user.getPassword());
        if(u != null){
            map.put("code",1);
            map.put("id",u.getId());
            session.setAttribute("user",u);
        }
        return map;
    }
}
