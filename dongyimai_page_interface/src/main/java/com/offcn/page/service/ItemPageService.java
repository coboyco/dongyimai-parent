package com.offcn.page.service;

public interface ItemPageService {

    //生成商品详情页
    public boolean genItemHtml(Long goodsId);

    //删除商品的详情页
    public boolean delItemHtml(Long[] ids);
}
