package com.offcn.page.service.impl;
import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbGoodsDesc;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper tbGoodsMapper;

    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private TbItemMapper tbItemMapper;


    //删除商品的详情页
    @Override
    public boolean delItemHtml(Long[] ids) {
        int x = 0;
        for (Long id : ids) {
            new File(pagedir,id+".html").delete();
            x++;
        }
        return x > 0 ? true : false;
    }

    //生成需要商品详细页
    @Override
    public boolean genItemHtml(Long goodsId) {


        try {
            Configuration configuration = freeMarkerConfig.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            Map mapModel = new HashMap();
            //根据商品id查询商品的基本信息
            TbGoods goods = tbGoodsMapper.selectByPrimaryKey(goodsId);
            mapModel.put("goods",goods);
            //根据商品id查询商品的扩展信息
            TbGoodsDesc goodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);
            mapModel.put("goodsDesc",goodsDesc);

            //查询面包屑栏的内容
            String itemCat1  = tbItemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2  = tbItemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3  = tbItemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            mapModel.put("itemCat1",itemCat1);
            mapModel.put("itemCat2",itemCat2);
            mapModel.put("itemCat3",itemCat3);

            //页面生成sku列表变量
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            //商品必须通过审核
            criteria.andStatusEqualTo("1");
            //商品id
            criteria.andGoodsIdEqualTo(goodsId);
            //按照降序排列
            example.setOrderByClause("is_default desc");
            //查询
            List<TbItem> itemList = tbItemMapper.selectByExample(example);
            System.err.println(itemList);
            mapModel.put("itemList",itemList);
            //写出
            Writer out = new FileWriter(new File(pagedir + goodsId + ".html"));
            template.process(mapModel,out);
            out.close();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (TemplateException e) {
            e.printStackTrace();
            return false;
        }


    }
}
