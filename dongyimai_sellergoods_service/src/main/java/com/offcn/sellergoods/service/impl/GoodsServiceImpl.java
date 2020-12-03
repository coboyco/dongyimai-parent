package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.Goods;
import com.offcn.entity.PageResult;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 商品服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper tbGoodsDescMapper;

	@Autowired
	private TbBrandMapper tbBrandMapper;

	@Autowired
	private TbItemCatMapper tbItemCatMapper;

	@Autowired
	private TbSellerMapper tbSellerMapper;

	@Autowired
	private TbItemMapper tbItemMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");//设置未申请状态
		//先插入goods基本信息表
		goodsMapper.insert(goods.getGoods());
		//再插入扩展表信息
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		tbGoodsDescMapper.insert(goods.getGoodsDesc());
		//在tb_item表插入sku信息
		//调用一个公用的方法用来存储sku信息
		TbGoodsDesc tbGoodsDesc = goods.getGoodsDesc();
		List<TbItem> itemList = goods.getItemList();
		//判断是否启用规格，如果过启用，就遍历规格集合，添加数据
		if("1".equals(goods.getGoods().getIsEnableSpec())){
			//循环遍历sku信息
			for (TbItem item : itemList) {
				//为sku信息添加标题
				String goodsName = goods.getGoods().getGoodsName();
				String spec = item.getSpec();
				Map map = JSON.parseObject(spec, Map.class);
				Set keySet = map.keySet();
				for (Object key : keySet) {
					goodsName += " " + map.get(key);
				}
				//将商品的标题设置
				item.setTitle(goodsName);
				//调用方法
				pubAddSku(goods,item);
				//插入操作
				tbItemMapper.insert(item);
			}
		}else{
			//没有启用规格情况
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());
			//设置商品那价格
			item.setPrice(goods.getGoods().getPrice());
			//设置一个默认库存
			item.setNum(99999);
			//设置是否默认
			item.setIsDefault("1");
			//设置是否启用
			item.setStatus("1");
			item.setSpec("{}");
			//调用设置sku属性方法
			pubAddSku(goods,item);
			//保存sku到数据库
			tbItemMapper.insert(item);

		}
	}

	//sku信息存储的的公共方法
	public void pubAddSku(Goods goods,TbItem tbItem){
		//设置spu商品编号
		tbItem.setGoodsId(goods.getGoods().getId());
		//商家编号
		tbItem.setSellerId(goods.getGoods().getSellerId());
		//商品分类编号
		tbItem.setCategoryid(goods.getGoods().getCategory3Id());
		//sku商品创建时间
		tbItem.setCreateTime(new Date());
		//更新时间
		tbItem.setUpdateTime(new Date());
		//品牌名称
		//根据【】品牌id，读取对应品牌信息
		TbBrand tbBrand = tbBrandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		if(tbBrand != null){
			//设置品牌名称到sku独享
			tbItem.setBrand(tbBrand.getName());
		}
		//分类名称
		//更加分类id，获取分类信息
		TbItemCat tbItemCat = tbItemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		if(tbItemCat != null){
			//设置分类名称到sku对象
			tbItem.setCategory(tbItemCat.getName());
		}
		//商家名称
		//根据商家编号，读取商家信息
		TbSeller tbSeller = tbSellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		if(tbSeller != null){
			tbItem.setSeller(tbSeller.getName());
		}
		//获取商品配图
		String itemImages = goods.getGoodsDesc().getItemImages();
		List<Map> list = JSON.parseArray(itemImages, Map.class);
		//判断图片集合不为空，有内容，提取第一张图片
		if(list != null && list.size() > 0){
			tbItem.setImage((String) list.get(0).get("url"));
		}

	}


	//抽取公共方法，保存sku
	public void saveItemList(Goods goods){
		//判断是否启用规格值为1，启用规格
		if("1".equals(goods.getGoods().getIsEnableSpec())) {
			//3、sku信息处理
			List<TbItem> itemList = goods.getItemList();
			//遍历sku集合
			for (TbItem item : itemList) {
				//获取spu商品标题
				String goodsName = goods.getGoods().getGoodsName();
				String jsonStr = item.getSpec();
				//转换规格的json字符串为集合
				Map specMap = JSON.parseObject(jsonStr, Map.class);
				//遍历规格map
				for (Object key : specMap.keySet()) {
					goodsName += " " + specMap.get(key);
				}
				//设置商品标题到sku对象
				item.setTitle(goodsName);

				//设置sku的属性值
				pubAddSku(goods,item);

				//把sku数据保存到数据库
				tbItemMapper.insert(item);
			}
		}else {

			//创建一个skuduixiang
			TbItem item = new TbItem();
			//设置sku名称
			item.setTitle(goods.getGoods().getGoodsName());
			//设置商品那价格
			item.setPrice(goods.getGoods().getPrice());
			//设置一个默认库存
			item.setNum(99999);
			//设置是否默认
			item.setIsDefault("1");
			//设置是否启用
			item.setStatus("1");
			item.setSpec("{}");
			//调用设置sku属性方法
			pubAddSku(goods,item);
			//保存sku到数据库
			tbItemMapper.insert(item);
		}
	}



	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//设置未申请状态:如果是经过修改的商品，需要重新设置状态
		goods.getGoods().setAuditStatus("0");
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		tbGoodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//删除掉sku的数据
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		//删除操作
		tbItemMapper.deleteByExample(example);
		//保存sku操作
		saveItemList(goods);


	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		TbGoodsDesc goodsDesc = tbGoodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(goodsDesc);
		//前台需要展示sku列表，获取sku数据
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = tbItemMapper.selectByExample(example);
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//进行逻辑删除
		for(Long id:ids){
			//查询商品信息
			TbGoods good = goodsMapper.selectByPrimaryKey(id);
			good.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(good);
		}
		List<TbItem> tbItemList = findGoodsByGoodIdAandStatus(ids, "1");
		for (TbItem item : tbItemList) {
			//修改sku信息状态为禁用
			item.setStatus("0");
			tbItemMapper.updateByPrimaryKey(item);
		}
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
						//排除idDelete为1的记录  为1表示删除，不能进行展示
			criteria.andIsDeleteIsNull();
		}
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	//更新商品的状态
	@Override
	public void updateState(Long[] id, String status) {
		//分为两部分，第一部分：修改Goods表中的状态，第二部分：同时同步到item表中
		for (Long d : id) {
			//获取商品信息
			TbGoods goods = goodsMapper.selectByPrimaryKey(d);
			goods.setAuditStatus(status);
			//更新商品信息实体
			goodsMapper.updateByPrimaryKey(goods);
			//更新sku列表的信息
			TbItemExample example = new TbItemExample();
			TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(d);
			List<TbItem> itemList = tbItemMapper.selectByExample(example);
			for (TbItem item : itemList) {
				//审核通过
				item.setStatus("1");
				tbItemMapper.updateByPrimaryKey(item);
			}
		}
	}

	//修改商品的上下架信息
	@Override
	public void updateIsMarketable(Long[] ids, String IsMarketable) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//设置上下架信息
			goods.setIsMarketable(IsMarketable);
			//更新上下架信息状态
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	//根据商品id和状态查询商品数据
	@Override
	public List<TbItem> findGoodsByGoodIdAandStatus(Long[] ids, String status) {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(ids));
		criteria.andStatusEqualTo(status);
		List<TbItem> itemList = tbItemMapper.selectByExample(example);
		return itemList;
	}
}
