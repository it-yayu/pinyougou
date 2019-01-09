package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CmsServiceImpl implements CmsService,ServletContextAware{
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Autowired
    private ServletContext servletContext;

    /**
     * 生成freemarker静态页面
     * @param goodsId
     * @param rootMap
     * @throws Exception
     */

    @Override
    public void createStaticPage(Long goodsId, Map<String, Object> rootMap) throws Exception {
        //创建模板对象
        Configuration configuration = freeMarkerConfig.getConfiguration();
        Template template = configuration.getTemplate("item.ftl");
        //创建writer流 指定页面生成的位置
        String path=goodsId+".html";
        //调用方法获取项目绝对路径
        String realPath = getRealPath(path);
        Writer out=new OutputStreamWriter(new FileOutputStream(new File(realPath)),"utf-8");
        //生成
        template.process(rootMap,out);
        //关闭流资源
        out.close();
    }
    private String getRealPath(String path){
        String realPath = servletContext.getRealPath(path);
        return realPath;
    }


    /**
     * 获取数据库数据
     * @param goodsId
     * @return
     */
    @Override
    public Map<String, Object> findGoodsData(Long goodsId) {
        Map<String,Object> resultMap=new HashMap<>();
        //获取商品数据
        Goods goods = goodsDao.selectByPrimaryKey(goodsId);
        //获取商品详情数据
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(goodsId);
        //获取库存数据
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(goodsId);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        //获取分类数据
       if(goods!=null){
           ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
           ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
           ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
           resultMap.put("itemCat1",itemCat1);
           resultMap.put("itemCat2",itemCat2);
           resultMap.put("itemCat3",itemCat3);


       }
        //封装对象
        resultMap.put("goods",goods);
        resultMap.put("goodsDesc",goodsDesc);
        resultMap.put("itemList",itemList);
        return resultMap;
    }



    /**
     * 由于当前项目是service项目没有springmvc来初始化servlet
     * 所以只能通过实现ServletConfigAware
     * 通过servletContext获取项目运行的绝对路径
     * @param servletContext
     */
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext=servletContext;

    }

}
