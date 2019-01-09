package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
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
     * 五级联动按照parentId查找数据
     * @param parentId
     * @return
     */
   @RequestMapping("/findByParentId")
    public List<ItemCat> findByParentId(Long parentId){
        return itemCatService.findByParentId(parentId);
    }

    /**
     * 五级联动后两级
     */
    @RequestMapping("/findOne")
   public ItemCat findOnee(Long id){
       return itemCatService.findOne(id);
   }

   @RequestMapping("findAll")
    public List<ItemCat> findAll(){
       return itemCatService.findAll();
   }
}
