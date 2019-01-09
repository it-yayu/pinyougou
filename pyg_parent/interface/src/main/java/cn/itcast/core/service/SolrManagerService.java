package cn.itcast.core.service;

public interface SolrManagerService {
    //往solr中添加通过审核的商品
    public void saveItemToSolr(Long id);
    //删除solr中的数据
    public void deleteItemFromSolr(Long id);
}
