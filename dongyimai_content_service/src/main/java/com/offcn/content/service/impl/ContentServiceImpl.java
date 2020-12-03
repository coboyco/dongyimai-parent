package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentService;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbContentMapper;
import com.offcn.pojo.TbContent;
import com.offcn.pojo.TbContentExample;
import com.offcn.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 内容服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//清除缓存，根据分类的广告清除相应的缓存
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//修改的可能是广告的基本信息，也包括属性广告属性分类，所以对修改的原分类和修改后的分类都进行缓存清除
		//查询修修改前的分类CategoryId
		Long categoryIdOri = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		//删除修改之前的缓存
		redisTemplate.boundHashOps("content").delete(categoryIdOri);
		contentMapper.updateByPrimaryKey(content);
		//如果分类CategoryId发生了改变，也把改变之后的CategoryId也删除了
		//查询修改之后的商品信息的分类CategoryId
		Long categoryIdAfter = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		if(categoryIdAfter != categoryIdOri){
			//因为分类CategoryId发生改变，所以要清除这个CategoryId的缓存
			redisTemplate.boundHashOps("content").delete(categoryIdAfter);
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbContent content = contentMapper.selectByPrimaryKey(id);
			Long categoryId = content.getCategoryId();
			//没删除一个商品，对应的缓存都会被删除
			redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);

		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	//根据广告分类id查询对应的广告信息

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		//查询之前先查询缓存
		List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
		if(contentList == null){
			TbContentExample example = new TbContentExample();
			Criteria criteria = example.createCriteria();
			criteria.andCategoryIdEqualTo(categoryId);
			criteria.andStatusEqualTo("1");
			example.setOrderByClause("sort_order");
			List<TbContent> tbContents = contentMapper.selectByExample(example);
			//将查询出的结果放入到缓存
			redisTemplate.boundHashOps("content").put(categoryId,tbContents);
		}else{
			System.out.println("从缓存中读取数据");
		}
		return contentList;

	}
}
