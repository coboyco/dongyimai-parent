package com.offcn.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SmsTemplate {

    @Value("${appcode}")
    private String appcode;

    @Value("${tpl_id}")
    private String  tpl_id;

    //定义短信网关主机地址
    private String host = "http://dingxin.market.alicloudapi.com";

    //发送程序
    //参数一：接收的手机号码 参数二：短信验证码
    public String smsSend(String mobile,String smscode){
        //定义发送短信的访问路径
        String path = "/dx/sendSms";

        //定义请求的方式
        String method = "POST";

        //创建一个请求头map集合
        Map<String,String> heads = new HashMap<>();
        //把身份验证信息封装到请求头中
        heads.put("Authorization","APPCODE " + appcode);

        //创建一个请求参数集合
        Map<String,String> querys = new HashMap<>();
        //请求的手机
        querys.put("mobile",mobile);
        //请求验证码参数
        querys.put("param","code:"+smscode);
        //请求模板编号
        querys.put("tpl_id",tpl_id);
        //请求body内容
        Map<String,String> bodys=new HashMap<String, String>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, heads, querys, bodys);
            HttpEntity entity = response.getEntity();
            String result=  EntityUtils.toString(entity,"utf-8");
            System.out.println("短信发送返回结果:"+result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
