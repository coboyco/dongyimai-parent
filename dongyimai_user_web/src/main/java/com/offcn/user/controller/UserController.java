package com.offcn.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.pojo.TbUser;
import com.offcn.user.service.UserService;
import com.offcn.utils.PhoneFormatCheckUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户表controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){
		return userService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows){
		return userService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user,String smscode){

		//判断用户输入的验证码和redis中哦验证码是否一样
		if(!userService.checkSmsCode(user.getPhone(),smscode)){
			return new Result(false,"验证码校验错误，请重新输入");
		}
		try {
			userService.add(user);
			//向消息中间件发送消息
            userService.sendUserEmail(user.getEmail(),"注册成功，欢迎使用！");
			return new Result(true, "增加成功,邮件已经发送！");
		} catch (Exception e) {
			e.printStackTrace();
					return new Result(false, "增加失败");
					}
					}

/**
 * 修改
 * @param user
 * @return
 */
@RequestMapping("/update")
public Result update(@RequestBody TbUser user){
		try {
		userService.update(user);
		return new Result(true, "修改成功");
		} catch (Exception e) {
		e.printStackTrace();
		return new Result(false, "修改失败");
		}
		}

/**
 * 获取实体
 * @param id
 * @return
 */
@RequestMapping("/findOne")
public TbUser findOne(Long id){
		return userService.findOne(id);
		}

/**
 * 批量删除
 * @param ids
 * @return
 */
@RequestMapping("/delete")
public Result delete(Long [] ids){
		try {
		userService.delete(ids);
		return new Result(true, "删除成功");
		} catch (Exception e) {
		e.printStackTrace();
		return new Result(false, "删除失败");
		}
		}

/**
 * 查询+分页
 * @param
 * @param page
 * @param rows
 * @return
 */
@RequestMapping("/search")
public PageResult search(@RequestBody TbUser user, int page, int rows  ){
		return userService.findPage(user, page, rows);
		}


//发送短信验证码
@RequestMapping("sendCode")
public Result sendCode(String phone){
		//验证手机号是否合法
		if(!PhoneFormatCheckUtils.isChinaPhoneLegal(phone)){
		return new Result(false,"手机号输入不合法");
		}

		//合法就创建随机生成树
		try {
		userService.createSmsCode(phone);
		return new Result(true,"验证码发送成功");
		} catch (Exception e) {
		e.printStackTrace();
		return new Result(false,"验证码发送失败");
		}

		}
		}
