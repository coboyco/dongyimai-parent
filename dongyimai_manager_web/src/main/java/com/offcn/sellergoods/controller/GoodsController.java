package com.offcn.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.entity.Goods;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Arrays;
import java.util.List;

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

	//@Reference
	//private ItemSearchService itemSearchService;

	/*@Reference(timeout = 8000)
	private ItemPageService itemPageService;*/

	@Autowired
	private Destination queueSolrDestination;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Destination queueDelSolrDestination;

	@Autowired
	private Destination topicPageDestination;

	@Autowired
	private Destination topicPageDeleteDestination;
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
	public PageResult findPage(int page, int rows){
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
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
			//itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			//通过发送消息到中间件，达到解耦的目的
			jmsTemplate.send(queueDelSolrDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			//删除商品的同时，同时把服务器上生成号的界面也给删除了,通过消息中间件操作
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
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
		return goodsService.findPage(goods, page, rows);		
	}

	//更新商品的状态
	@RequestMapping("/updateState")
	public Result updateState(Long[] id, String status){
		try {
			goodsService.updateState(id,status);

			//随着更新商品的状态，同时也要同时更新到solr中以便用户及时的看到
			if(status.equals("1")){
				//根据数组id和状态查询用户更新的数据
				List<TbItem> tbItemList = goodsService.findGoodsByGoodIdAandStatus(id, status);
				//调用接口更新索引库
				if(tbItemList.size() > 0){
					//itemSearchService.importList(tbItemList);
					//数据更新完成，需要向solr中更新数据，使用消息中间件来发送消息，解耦合
					//将集合转换为字符串
					String jsonString = JSON.toJSONString(tbItemList);
					//发送信息 参数一是目的地   参数二是要发送的数据
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(jsonString);
						}
					});
					//调用静态页面生成服务，生成对应的静态界面
					for (Long d : id) {
						/*itemPageService.genItemHtml(d);*/
						//采用发布订阅模式
						jmsTemplate.send(topicPageDestination, new MessageCreator() {
							@Override
							public Message createMessage(Session session) throws JMSException {
								return session.createTextMessage(d+"");
							}
						});

					}
				}else{
					System.out.println("没有更新数据，不需要更新索引库");
				}
			}

			return new Result(true,"审核通过！");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"操作失败!");
		}
	}


	/*//生成静态测试网页
	@RequestMapping("/genHtml")
	public void genHtml(Long goodsId){
		itemPageService.genItemHtml(goodsId);
	}*/

}
