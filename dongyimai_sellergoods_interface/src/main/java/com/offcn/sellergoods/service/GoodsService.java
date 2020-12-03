package com.offcn.sellergoods.service;

import com.offcn.entity.Goods;
import com.offcn.entity.PageResult;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;

import java.util.List;

/**
 * 商品服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(Goods goods);


	/**
	 * 修改
	 */
	public void update(Goods goods);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);


	//更细商品的状态
	public void updateState(Long[] id,String status);

	//修改商品的上下架状态
	public void updateIsMarketable(Long[] ids, String IsMarketable);

	//根据ids集合和状态，查询商品数据
	public List<TbItem> findGoodsByGoodIdAandStatus(Long[] ids, String status);
}
