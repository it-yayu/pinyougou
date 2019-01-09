package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.CmsService;
import cn.itcast.core.service.GoodsService;
import cn.itcast.core.service.SolrManagerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    GoodsService goodsService;
   /* @Reference
    SolrManagerService solrManagerService;*/
/*    @Reference
    private CmsService cmsService;*/
    /**
     * 分页查询所有待审核的商品
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody Goods goods,Integer page,Integer rows){
        //获取登录的用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(name);
         return goodsService.findByPage(goods,page,rows);
    }

    /**
     * 详情的数据展示
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id){
        return goodsService.findOne(id);
    }

    /**
     * 批量删除操作
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            if(ids!=null){
                for (Long id : ids) {
                    //根据商品id逻辑删除数据库数据
                    goodsService.delete(id);
                    /*//根据商品id删除solr中的数据
                    solrManagerService.deleteItemFromSolr(id);*/

                }
            }
            return  new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){
        try {
            if (ids!=null){
                for (Long id : ids) {
                    //根据商品id到数据库中改变商品的上架状态
                    goodsService.updateStatus(id,status);
                    //对于审核通过的商品 根据商品id获取库存数据放入solr中 供消费者查询使用
                   /* if("1".equals(status)){
                        solrManagerService.saveItemToSolr(id);
                        //根据商品id查询商品详细信息 根据详细信息生成静态化页面
                        Map<String, Object> goodsData = cmsService.findGoodsData(id);
                        cmsService.createStaticPage(id,goodsData);
                    }
*/
                }
            }
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    /**
     * 测试页面生成
     * @param goodsId
     * @return
     */
    /*@RequestMapping("/testPage")
    public Boolean testCreatePage(Long goodsId){
        try {
            Map<String, Object> goodsData = cmsService.findGoodsData(goodsId);
            cmsService.createStaticPage(goodsId,goodsData);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }*/
}
