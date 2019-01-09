package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.entity.SpecEntity;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {
    @Reference
    SpecificationService specificationService;

    /**
     * 高级分页查询
     *
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody Specification spec, Integer page, Integer rows) {
        return specificationService.findPage(spec, page, rows);
    }

    /**
     * 添加规格和规格项
     */
    @RequestMapping("/add")
    public Result add(@RequestBody SpecEntity specEntity) {
        try {
            specificationService.save(specEntity);
            return new Result(true, "添加成功!");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result(false, "添加失败!");
        }
    }

    /**
     * 修改数据回显
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public SpecEntity findOne(Long id) {
        return specificationService.findOne(id);
    }

    /**
     * 修改保存数据
     */
    @RequestMapping("/update")
    public Result uptate(@RequestBody SpecEntity specEntity) {
        try {
            specificationService.update(specEntity);
            return new Result(true, "添加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败!");
        }
    }

    /**
     * 批量删除
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            specificationService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * select2 查询所有的关联规格项
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        return specificationService.selectOptionList();
    }
}
