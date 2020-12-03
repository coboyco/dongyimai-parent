package com.offcn.user;

import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailImpl implements UserDetailsService {

   private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //创建一个集合，赋予用户权限
        List<GrantedAuthority> list = new ArrayList<>();
        //向集合中添加权限
        list.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //根据username获取商家用户信息
        TbSeller tbSeller = sellerService.findOne(username);
        //判断tbSeller是否为空
        if(tbSeller != null){
            //判断审核是否通过 通过status为1
            if(tbSeller.getStatus().equals("1")){
                //将用户名密码和权限返回给springSecurity，让springSecurity帮助我们去P那U盾那用户的用户名和密码是否一致
                return new User(username,tbSeller.getPassword(),list);
            }else {
                return null;
            }
        }
        return null;
    }
}
