package cn.itcast.core.controller;

import cn.itcast.core.service.SearchService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemsearch")
public class SearchController {
    @Reference
    private SearchService searchService;

    /**
     * 关键字的查询
     * @param paramMap
     * @return
     */
    @RequestMapping("/search")
    public Map<String,Object> search(@RequestBody Map paramMap){
        return searchService.search(paramMap);
    }
}
