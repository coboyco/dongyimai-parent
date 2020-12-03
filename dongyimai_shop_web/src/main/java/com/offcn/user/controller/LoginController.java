package com.offcn.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("name")
    public Map name(){
        Map map = new HashMap();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName",username);
        return map;
    }
}
