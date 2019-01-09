package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    BrandService brandService;

    /**
     * 查询所有的管理商品
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<Brand> findAll() {
        List<Brand> brandList = brandService.findAll();
        return brandList;
    }

    /**
     * 分页查询所有数据
     *
     * @param page
     * @param rows
     * @returnf findPage
     */

    @RequestMapping("/findByPage")
    public PageResult findPage(Integer page, Integer rows) {
        return brandService.findPage(page, rows);
    }

    /**
     * 添加商品
     * add
     */
    @RequestMapping("/save")
    public Result add(@RequestBody Brand brand) {
        try {
            brandService.add(brand);
            return new Result(true, "增加成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 根据商品id查找单个的商品
     *
     * @param id
     * @return findOne
     */
    @RequestMapping("/findById")
    public Brand fndOne(Long id) {
        return brandService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand) {
        try {

            brandService.update(brand);
            return new Result(true, "更新成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result(false, "更新失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            brandService.delect(ids);
            return new Result(true, "删除成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    @RequestMapping("/search")
    public PageResult search(@RequestBody Brand brand, Integer page, Integer rows) {
        return brandService.findPage(brand, page, rows);
    }

    /**
     * select2 框架查询商品
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        return brandService.selectOptionList();
    }


}
