package com.offcn.solrutil;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    //导入商品数据
    public void importItemData(){
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        //查询出所有的商品数据
        List<TbItem> itemList = itemMapper.selectByExample(example);
        //将查询出来的sku信息存入到solr中
        for (TbItem item : itemList) {
            String itemSpec = item.getSpec();
            Map<String,String> specMap = JSON.parseObject(itemSpec, Map.class);
            //需要将key中文形式转换成拼音
            HashMap<String,String> specMapPY = new HashMap<>();
            //遍历中的map集合
            for (String key : specMap.keySet()) {
                //将specMap中的数据存方到specMapPY中，此时key已经变成英文了
                specMapPY.put(Pinyin.toPinyin(key,"").trim().toLowerCase(),specMap.get(key));
            }
            item.setSpecMap(specMapPY);
        }

        //调用solr模板添加数据
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();

    }

    public static void main(String[] args) {
        //加载spring
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");

        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");

        solrUtil.importItemData();

    }
}
