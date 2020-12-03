package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        //判断如果用户输入的关键字中空格，此时需要进行处理
        String keywords = (String) searchMap.get("keywords");
        if(keywords != null && keywords.indexOf(" ") > 0){
            //对字符串进行处理
            String keyword = keywords.replaceAll(" ", "");
            //重新进行封装到查询map集合
            searchMap.put("keywords",keyword);
        }

        Map<String, Object> map = new HashMap<>();
       /* //创建查询器对象
        //        SimpleQuery query = new SimpleQuery();
        //        //创建条件查询对象
        //        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //        //将查询对象添加到查询器对象
        //        query.addCriteria(criteria);
        //        //执行
        //        ScoredPage<TbItem> queryForPage = solrTemplate.queryForPage(query, TbItem.class);
        //        List<TbItem> itemList = queryForPage.getContent();
        //        //将查询到的结果封装到map集合*/
        map.putAll(searchList(searchMap));

        //根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        if(!"".equals(searchMap.get("category"))){

                 //获取前端传递分类名称，作为查询条件查询对应品牌和规格
                map.putAll(searchBrandSpecList((String) searchMap.get("category")));

        }else {
            if(categoryList != null && categoryList.size() > 0){
                //根据分类查询品牌列表和规格以及规格选项
                map.putAll(searchBrandSpecList((String)categoryList.get(0)));
            }
        }

        return map;
    }

    //根据查询关键字，对查询结果进行高亮显示
    private Map searchList(Map searchMap){
        Map map = new HashMap<>();
        //创建一个支持高亮的查询对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //设定需要高亮处理的字段(不能是复制域) 设置高亮选项
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        //为高亮设置前缀和后缀html样式
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        //将高亮选项的配置存放到高亮查询器中
        query.setHighlightOptions(highlightOptions);
        //设定查询条件，根据关键字查询
        //创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //关联条件查询对象到查询器对象
        query.addCriteria(criteria);

        //天剑过滤条件1，按照分类进行过滤
        //判断分类过滤查询条件是否不为空白
        if(!"".equals(searchMap.get("category"))){
            //创建一个查询条件
            Criteria criteriaCategory = new Criteria("item_category").is(searchMap.get("category"));
            //创建一个过滤器对象
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(criteriaCategory);
            //关联过滤器到查询器对象
            query.addFilterQuery(filterQuery);
        }

        //过滤条件2，按照品牌进行过滤
        //判断品牌过滤查询条件是否不为空白
        if(!"".equals(searchMap.get("brand"))){
            //创建一个查询条件
            Criteria criteriaBrand = new Criteria("item_brand").is(searchMap.get("brand"));
            //创建一个过滤器对象
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(criteriaBrand);
            //关联过滤器对象到查询器对象
            query.addFilterQuery(filterQuery);
        }

        //添加过滤条件三：按照规格和规格选项，进行过滤
        if(searchMap.get("spec") != null){
           Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");

            for (String key : specMap.keySet()) {
                Criteria criteriaSpec = new Criteria("item_spec_"+ Pinyin.toPinyin(key,"").toLowerCase().trim()).is(specMap.get(key));
                //创建一个过滤器对象
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(criteriaSpec);
                //关联过滤器对象到查询器对象
                query.addFilterQuery(filterQuery);
            }

        }

        //根据价格进行过滤
        if(!"".equals(searchMap.get("price"))){
            //获取由前端传过来价格区间  0-500 500-1000 ... 3000-*
            String priceStr = (String) searchMap.get("price");
            String[] priceSpilt = priceStr.split("-");
            //判断如果第一个是非0的话 进行条件查询
            if(!priceSpilt[0].equals("0")){
                Criteria criteriaPrice0 = new Criteria("item_price").greaterThan(priceSpilt[0]);
                SimpleFilterQuery queryPrice = new SimpleFilterQuery(criteriaPrice0);
                query.addFilterQuery(queryPrice);
            }

            //判断价格元素数组第二个是否为 *  如果为*就不在执行，只执行第一个
            if(!priceSpilt[1].equals("*")){
                Criteria criteriaPrice1 = new Criteria("item_price").lessThan(priceSpilt[1]);
                SimpleFilterQuery queryP = new SimpleFilterQuery(criteriaPrice1);
                query.addFilterQuery(queryP);
            }
        }


        //分页处理
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo == null){
            //默认当前页
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageSize == null){
            //默认分页显示的数量
            pageSize = 20;
        }
        //从第几条记录开始查
        query.setOffset((pageNo - 1) * pageSize);
        //查询多少条
        query.setRows(pageSize);


        //排序条件
        //排序  ASC  DESC
        String sort = (String) searchMap.get("sort");
        //要排序的字段
        String sortFiled = (String) searchMap.get("sortFiled");
        //判断
        if(!"".equals(sortFiled) && sortFiled != null){
            //判断sort是升序还是降序
            if(sort.equals("ASC")){
                //此时是升序
                Sort sortASC = new Sort(Sort.Direction.ASC, "item_" + sortFiled);
                query.addSort(sortASC);
            }

            if(sort.equals("DESC")){
                //此时是降序
                Sort sortDESC = new Sort(Sort.Direction.DESC, "item_" + sortFiled);
                query.addSort(sortDESC);
            }
        }

        //发出带高亮数据查询请求
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //获取高亮集合入口
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        //遍历高亮集合
        for (HighlightEntry<TbItem> highlightEntry : highlightEntryList) {
            //获取基本数据对象
            TbItem item = highlightEntry.getEntity();
            if(highlightEntry.getHighlights().size() > 0
              && highlightEntry.getHighlights().get(0).getSnipplets().size()>0){
                List<HighlightEntry.Highlight> highlightList=  highlightEntry.getHighlights();
                List<String> snipplets = highlightList.get(0).getSnipplets();
                //获取第一个高亮字段对应的高亮结果，设置到商品主题
                item.setTitle(snipplets.get(0));
            }
        }
        map.put("rows",page.getContent());
        //将分页的数据返回  总页数  总的记录数
        map.put("totalPages",page.getTotalPages());
        map.put("total",page.getTotalElements());
        return map;
    }

    //查询分类列表
    private List searchCategoryList(Map searchMap){
        List list = new ArrayList();
        //创建查询器
        SimpleQuery query = new SimpleQuery();
        //根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //获得分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            //将分组结果的名称封装到list中
            list.add(entry.getGroupValue());
        }
        return list;
    }

    //查询品牌和规格列表
    private Map searchBrandSpecList(String category){
        Map  map = new HashMap();
        //根据传入的分类名称获取模板id
        System.out.println("分类名称:"+category);
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        //判断
        if(typeId != null){
            //根据模板id查询分类列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            //将查询到的数据存储到mao中
            map.put("brandList",brandList);

            //根据模板id查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            //将查询到的数据存储到ma中
            map.put("specList",specList);
        }
        return map;
    }

    //更新索引库
    @Override
    public void importList(List<TbItem> list) {
        for (TbItem item : list) {
            String itemSpec = item.getSpec();
            //{"机身内存":"16G","网络":"联通3G"}
            Map<String,String> map = JSON.parseObject(itemSpec, Map.class);

            Map<String,String> specMap = new HashMap<>();
            //对其key进行拼音化
            for (String key : map.keySet()) {
                specMap.put("item_"+ Pinyin.toPinyin(key,"").toLowerCase().trim(),map.get(key));
            }
            //对item尽心新的设定spec
            item.setSpecMap(specMap);
        }

        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    //删除索引库中不需要的数据
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        SimpleQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
