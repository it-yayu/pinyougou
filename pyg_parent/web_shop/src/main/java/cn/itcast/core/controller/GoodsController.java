package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import cn.itcast.core.service.SolrManagerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
   private GoodsService goodsService;
    @Reference
   private SolrManagerService solrManagerService;
    @RequestMapping("/add")
    public Result add(@RequestBody GoodsEntity goodsEntity){
        try {
            //获取登录名
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //设置商品添加的用户名  (卖家id)
            goodsEntity.getGoods().setSellerId(name);
            goodsService.add(goodsEntity);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }

    /**
     * 高级分页查询
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody Goods goods,Integer page,Integer rows ){
        //获取登录的用户姓名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(name);
        return goodsService.findByPage(goods,page,rows);
    }

    /**
     * 修改数据回显
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id){

       return goodsService.findOne(id);
    }

    /**
     * 修改数据数据保存
     * @param goodsEntity
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody GoodsEntity goodsEntity){
        try {
            //获取登录的用户名
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            goodsEntity.getGoods().setSellerId(name);
            goodsService.update(goodsEntity);
            return  new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }


    }

    /**
     * 删除
     *
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            if(ids!=null){
                for (Long id : ids) {
                    //修改数据库的数据状态
                    goodsService.delete(id);
                    //删除solr中的数据
                    solrManagerService.deleteItemFromSolr(id);
                }
            }
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }

    }
}
