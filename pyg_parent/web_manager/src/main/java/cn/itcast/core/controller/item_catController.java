package cn.itcast.core.controller;


import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class item_catController {
   @Reference
    ItemCatService itemCatService;

    /**
     * 分类管理的数据展示
     * @param parentId
     * @return
     */
   @RequestMapping("/findByParentId")
    public List<ItemCat> findByParentId(Long parentId){
        return itemCatService.findByParentId(parentId);
    }

    //商品审核一级二级三级分类
    @RequestMapping("/findAll")
    public List<ItemCat> findAll(){
     return   itemCatService.findAll();
    }
}
