package com.offcn.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("login")
public class LoginController {

    //运营商管理人员进入后台后要显示的名称
    @RequestMapping("name")
    public Map name(){
        //map用来存储名字信息
        Map map = new HashMap();
        //从springSecurity中获取登录者的姓名信息
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //将数据存储到map中
        map.put("loginName",username);
        //将数据返回
        return map;
    }
}
