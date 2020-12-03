package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import com.offcn.utils.IdWorker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制层
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AliPayService aliPayService;

    @Reference(timeout=10000)
    private OrderService orderService;

    @RequestMapping("createNative")
    public Map createNative() {
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(userId);
        //到redis查询支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        //判断支付日志存在
        if(payLog!=null){
            Map map = aliPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
            System.out.println(map);
            return aliPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else{
            return new HashMap();
        }
    }


    /**
     * 查询支付状态
     *
     * @param out_trade_no
     * @return
     */

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;

        int x = 0;
        while (true) {
            //调用查询接口
            Map<String, String> map = null;
            try {
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("调用查询接口失败");
            }

            if (map == null) {
                //出错
                result = new Result(false, "支付出错");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_SUCCESS")) {
                //如果成功
                result = new Result(true, "支付成功");
                //修改订单状态
                orderService.updateOrderStatus(out_trade_no, map.get("trade_no"));
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_CLOSED")) {
                //如果成功
                result = new Result(true, "未付款交易超时，或支付完成后全额退款");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_FINISHED")) {//如果成功
                result = new Result(true, "交易结束，不可退款");
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if(x >= 100){
                result=new  Result(false, "二维码超时");
                break;
            }

        }
        return result;

    }
}