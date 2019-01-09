package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.util.Constans;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;


import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {
    /**
     * solr关键字查询
     * 返回的数据有 查询到的集合  当前页   每页展示多少条数据 总记录数  总页数
     *
     * @param paramMap
     * @return
     */
    //注入solrtemplate模板对象
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map paramMap) {
        //1. 根据查询参数, 到solr中分页, 高亮, 过滤, 排序查询
        Map<String, Object> resultMap = highlightSearch(paramMap);
        //2. 根据查询参数, 到solr中获取对应的分类结果集, 由于分类重复, 所以需要分组去重
        List<String> groupCategoryList=findGroupCatagoryList(paramMap);
        resultMap.put("categoryList",groupCategoryList);
        //3. 判断paramMap传入参数中是否有分类名称
        String category = String.valueOf(paramMap.get("category"));
        if(category!=null&&!"".equals(category)){
            //如果有就根据分类查询对应的品牌集合 和规格集合
            Map brandListAndSpecList = findBrandListAndSpecList(category);
            resultMap.putAll(brandListAndSpecList);
        }else{
            //如果没有默认根据第一个去查询对应的品牌集合和规格集合
            Map brandListAndSpecList = findBrandListAndSpecList(groupCategoryList.get(0));
            resultMap.putAll(brandListAndSpecList);

        }
        return resultMap;
    }



    /**
     * 根据关键字, 分页, 高亮, 过滤, 排序查询, 并且将查询结果返回
     */
    private Map<String,Object> highlightSearch(Map paramMap){
        /**
         * 1.获取查询条件
         */
        //获取查询关键字
        String keywords = String.valueOf(paramMap.get("keywords"));
        if(keywords!=null&&!"".equals(keywords)){
           keywords = keywords.replace(" ", "");
        }
        //获取当前页
        Integer pageNo = Integer.parseInt(String.valueOf(paramMap.get("pageNo")));
        //获取每页展示多少条数据
        Integer pageSize = Integer.parseInt(String.valueOf(paramMap.get("pageSize")));
       //页面点击的分类过滤条件
        String category=String.valueOf(paramMap.get("category"));
        //页面点击的品牌过滤条件
        String brand = String.valueOf(paramMap.get("brand"));
        //页面点击的规格过滤条件
        String spec = String.valueOf(paramMap.get("spec"));
        //页面点击的价格过滤条件
        String price =String.valueOf(paramMap.get("price"));
        //页面点击的价格排序条件
        String sortType = String.valueOf(paramMap.get("sort"));
        //页面点击的排序域
        String sortField = String.valueOf(paramMap.get("sortField"));


        /**
         * 2.封装查询对象
         *
         */
        //创建查询高亮查询对象
        HighlightQuery query=new SimpleHighlightQuery();
        //创建查询条件
        Criteria criteria=new Criteria("item_keywords").is(keywords);
        //将查询条件访日查询对象中
        query.addCriteria(criteria);


        if (pageNo == null || pageNo <= 0) {
            pageNo = 1;
        }
        //设置开始查询页
        query.setOffset((pageNo - 1) * pageSize);
        //设置每页查询到少条数据
        query.setRows(pageSize);

        //设置高亮域
        HighlightOptions highlightOptions = new HighlightOptions();

        highlightOptions.addField("item_title");
        //设置高亮域的前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //设置高亮域的后缀
        highlightOptions.setSimplePostfix("</em>");
        //将高亮选项放入查询中
        query.setHighlightOptions(highlightOptions);

        /**
         * 过滤查询
         */
        //根据分类过滤查询
        if(category!=null&&!"".equals(category)){
            //创建过滤查询对象
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            //创建查询条件
            Criteria filterCriteria = new Criteria("item_category").is(category);
            //将查询条件放入过滤对象中
            filterQuery.addCriteria(filterCriteria);
            //将过滤对象放入查询对象中
            query.addFilterQuery(filterQuery);
        }
        //根据品牌过滤查询
        if(brand!=null && !"".equals(brand)){
            //创建过滤查询对象
            FilterQuery filterQuery = new SimpleFilterQuery();
            //创建查询条件
            Criteria filterCriteria = new Criteria("item_brand").is(brand);
            //将查询条件放入过滤对象中
            filterQuery.addCriteria(filterCriteria);
            //将过滤对象放入查询对象中
            query.addFilterQuery(filterQuery);
        }


        //根据规格过滤查询 spec中的数据格式{网络:4g,内存大小:168g}
        if(spec!=null && !"".equals(spec)){
            Map<String,String> specMap= JSON.parseObject(spec,Map.class);
            if(specMap!=null&&specMap.size()>0){
                Set<Map.Entry<String, String>> entries = specMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    //创建过滤查询对象
                    SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                    //创建查询条件
                    Criteria filterCriteria = new Criteria("item_spec_"+entry.getKey()).is(entry.getValue());
                    //将查询条件放入过滤对象中
                    filterQuery.addCriteria(filterCriteria);
                    //将过滤对象放入查询对象中
                    query.addFilterQuery(filterQuery);
                }
            }
        }

        /**
         * 根据价格进行过滤
         */
        if(price!=null&&!"".equals(price)){
            //对价格范文进行切分 获取最低价格和最高价格
            String[] split = price.split("-");
            if(split!=null&&split.length==2){
                //说明大于等于最小值
                if(!"0".equals(split[0])){
                    //创建过滤对象
                    SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                    //创建过滤条件
                    Criteria filterCriteria=new Criteria("item_price").greaterThan(split[0]);
                    //将过滤条件放入过滤对象中
                    filterQuery.addCriteria(filterCriteria);
                    //将过滤对象放入查询对象中
                    query.addFilterQuery(filterQuery);

                }
                //说明小于等于最大值
                if(!"*".equals(split[1])){
                    //创建过滤对象
                    SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                    //创建过滤条件
                    Criteria filterCriteria=new Criteria("item_price").lessThan(split[1]);
                    //将过滤条件放入过滤对象中
                    filterQuery.addCriteria(filterCriteria);
                    //将过滤对象放入查询对象中
                    query.addFilterQuery(filterQuery);
                }
            }
        }

        /**
         *添加排序条件
         */
        if(sortType!=null&&!"".equals(sortType)&&sortField!=null&&!"".equals(sortField)){
            if("ASC".equals(sortType)){
                //创建排序对象
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                //将排序对象放入查询对象中
                query.addSort(sort);
            }
            if("DESC".equals(sortType)){
                //创建排序对象
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                //将排序对象放入查询对象中
                query.addSort(sort);
            }
        }

        /**
         * 3.高亮查询结果并返回
         */
        HighlightPage<Item> items = solrTemplate.queryForHighlightPage(query, Item.class);
        //获取带高亮的集合
        List<HighlightEntry<Item>> highlighted = items.getHighlighted();
        List<Item> itemList=new ArrayList<>();
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            //获取到不带高亮的实体对象
            Item item = itemHighlightEntry.getEntity();
            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();
            if(highlights!=null&&highlights.size()>0){
                //获取高亮标题集合
                List<String> snipplets = highlights.get(0).getSnipplets();
                if(snipplets!=null&&snipplets.size()>0){
                    //获取到高亮标题
                    String s = snipplets.get(0);
                    //设置高亮标题
                    item.setTitle(s);
                }
            }
            itemList.add(item);



        }

        //封装返回有用的数据 items.getTotalPages(); items.getContent(); items.getTotalElements();
        Map<String, Object> resultMap = new HashMap<>();
        //查询到的结果集数据
        resultMap.put("rows", itemList);
        //查询到的总页数
        resultMap.put("totalPages", items.getTotalPages());
        //查询到的总条数
        resultMap.put("total", items.getTotalElements());

        return resultMap;
    }



    /**
     * 根据关键字到solr中去查找对应的分类集合,要分组去重
     *
     * 1.(获取关键字)根据关键字创建高亮查询对象
     * 2.根据关键字创建分组查询对象
     * 3.获取分组查询分类集合
     * 4.遍历集合提取数据返回
     * @param paramMap
     * @return
     */
    private List<String> findGroupCatagoryList(Map paramMap){
        List<String> resultList=new ArrayList<>();
        //获取查询关键字
        String keywords = String.valueOf(paramMap.get("keywords"));
        if(keywords!=null&&!"".equals(keywords)){
            keywords=keywords.replace(" ","");
        }
        //创建高亮查询对象
         Query query=new SimpleQuery();
        //创建查询条件
        Criteria criteria=new Criteria("item_keywords").is(keywords);
        //将查询条件放入查询对象中
        query.addCriteria(criteria);
        //创建分组查询对象
        GroupOptions groupOptions = new GroupOptions();
        //设置根据分类进行分组
         groupOptions.addGroupByField("item_category");
        //将分组对对象放入查询队对象中
        query.setGroupOptions(groupOptions);
        //分组查询分类集合
        GroupPage<Item> items = solrTemplate.queryForGroupPage(query, Item.class);
        //获取结果集中分类的集合
        GroupResult<Item> item_category = items.getGroupResult("item_category");
        //分类集合中获取实体集合
        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();
        //遍历实体集合
        for (GroupEntry<Item> groupEntry : groupEntries) {
            String groupValue = groupEntry.getGroupValue();
            resultList.add(groupValue);
        }
        return resultList;
    }

    /**
     * 根据分类名称到redis中查询对应的品牌集合和规格集合
     * @param categoryName
     * @return
     */
    private Map findBrandListAndSpecList(String categoryName){
        System.out.println(categoryName);
        //根据分类名称到redis中查询模板id
    Long templateId=(Long)redisTemplate.boundHashOps(Constans.CATEGORY_LIST_REDIS).get(categoryName);
        //根据模板id 到redis中查询对应的品牌集合
     List<Map> brandList=(List<Map>)redisTemplate.boundHashOps(Constans.BRAND_LIST_REDIS).get(templateId);
        //根据模板id 到redis中查询对应的规格集合
      List<Map>specList=(List<Map>) redisTemplate.boundHashOps(Constans.SPEC_LIST_REDIS).get(templateId);
      //将品牌集合和规格集合封装到map集合中返回
        Map resultMap=new HashMap();
        resultMap.put("brandList",brandList);
        resultMap.put("specList",specList);
        return resultMap;
    }
}
