package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.ContentCategory;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;

import cn.itcast.core.service.ContentCategoryService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/contentCategory")
public class ContentCategoryController {
    @Reference
    private ContentCategoryService categoryService;


    @RequestMapping("/add")
    public Result add(@RequestBody ContentCategory contentCategory) {
        try {
            categoryService.add(contentCategory);
            return new Result(true, "增加成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result(false, "增加失败");
        }
    }


    @RequestMapping("/findOne")
    public ContentCategory fndOne(Long id) {
        return categoryService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody ContentCategory contentCategory) {
        try {

            categoryService.update(contentCategory);
            return new Result(true, "更新成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result(false, "更新失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            categoryService.delect(ids);
            return new Result(true, "删除成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    @RequestMapping("/search")
    public PageResult search(@RequestBody ContentCategory contentCategory, Integer page, Integer rows) {
        return categoryService.findPage(contentCategory, page, rows);
    }
    @RequestMapping("/findAll")
    public List<ContentCategory> findAll(){
        List<ContentCategory> list = categoryService.findAll();
        return list;

    }

}
