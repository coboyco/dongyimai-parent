package com.offcn.sellergoods.controller;
import java.util.List;

import com.offcn.entity.Goods;
import com.offcn.sellergoods.service.GoodsDescService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbGoods;
import com.offcn.sellergoods.service.GoodsService;

import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
/**
 * 商品controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.getGoods().setSellerId(sellerId);//设置商家ID
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		//判断当前修改操作是否为当前登录的商家
		//获取当前登录的商家的sellerId
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		Goods good = goodsService.findOne(goods.getGoods().getId());
		if(!good.getGoods().getSellerId().equals(sellerId) || !goods.getGoods().getSellerId().equals(sellerId)){
			return  new Result(false,"权限不够，非法操作，失败！");
		}

		try {
			goodsService.update(goods);
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
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
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
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		//添加查询条件 当前用户只能查询自己的商品情况，不能对查询其他用户的查询情况
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.setSellerId(sellerId);
		return goodsService.findPage(goods, page, rows);		
	}

	//更新商品上下架信息
	@RequestMapping("updateIsMarketable")
	public Result updateIsMarketable(Long[] ids, String IsMarketable) {
		if(ids.length == 0){
			return new Result(false,"操作不合法，请勾选!");
		}
		try {
			goodsService.updateIsMarketable(ids,IsMarketable);
			if(IsMarketable.equals("1")){
				return new Result(true,"商品上架");
			}else {
				return new Result(true,"商品下架");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"上下架状态更新失败");

		}
	}
	
}
