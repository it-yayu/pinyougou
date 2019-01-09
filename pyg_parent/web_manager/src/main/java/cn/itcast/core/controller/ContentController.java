package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.Content;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/content")
public class ContentController {
    @Reference
    private ContentService contentService;


    @RequestMapping("/add")
    public Result add(@RequestBody Content Content) {
        try {
            contentService.add(Content);
            return new Result(true, "增加成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result(false, "增加失败");
        }
    }


    @RequestMapping("/findOne")
    public Content fndOne(Long id) {
        return contentService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody Content Content) {
        try {

            contentService.update(Content);
            return new Result(true, "更新成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result(false, "更新失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            contentService.delect(ids);
            return new Result(true, "删除成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    @RequestMapping("/search")
    public PageResult search(@RequestBody Content Content, Integer page, Integer rows) {
        return contentService.findPage(Content, page, rows);
    }
    @RequestMapping("/findAll")
    public List<Content> findAll(){
        List<Content> list = contentService.findAll();
        return list;

    }

}
