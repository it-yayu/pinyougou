package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import javax.management.Query;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    GoodsDao goodsDao; //商品对象数据表
    @Autowired
    GoodsDescDao goodsDescDao;//商品描述数据表
    @Autowired
    ItemDao itemDao;//库存集合数据表
    @Autowired
    BrandDao brandDao;
    @Autowired
    ItemCatDao itemCatDao;
    @Autowired
    SellerDao sellerDao;
    //注入消息发送工具类
    @Autowired
    private JmsTemplate jmsTemplate;
    //注入发布订阅者模式 商品存入数据库和生成商品静态页面
    @Autowired
    private ActiveMQTopic topicPageAndSolrDestination;
    //注入一对一模式 商品删除使用
    @Autowired
    private ActiveMQQueue queueSolrDeleteDestination;

    /**
     * sku商品保存
     * @param goodsEntity
     */
    @Override
    public void add(GoodsEntity goodsEntity) {
        //1.保存商品对象
        //设置商品初始化状态

        goodsEntity.getGoods().setAuditStatus("0");
        goodsDao.insertSelective(goodsEntity.getGoods());
        //2.保存商品描述对象
        //商品对象的主键是自增 商品描述对象的主键就是商品对象的主键 (xml文件返回自增的主键)
        goodsEntity.getGoodsDesc().setGoodsId(goodsEntity.getGoods().getId());
        goodsDescDao.insertSelective(goodsEntity.getGoodsDesc());
        //3.保存库存集合对象
        insert(goodsEntity);

    }

    /**
     * 高级分页查询
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult findByPage(Goods goods, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        GoodsQuery query = new GoodsQuery();
        GoodsQuery.Criteria criteria = query.createCriteria();
        if(goods!=null){
            //名字模糊查询
            if(goods.getGoodsName()!=null&&!"".equals(goods.getGoodsName())){
                criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
            }
            //商家名模糊查询
            if (goods.getSellerId() != null&&!"".equals(goods.getSellerId())
                    && !"admin".equals(goods.getSellerId()) && !"wc".equals(goods.getSellerId())) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            //根据商品的状态查询
            if(goods.getAuditStatus()!=null&&!"".equals(goods.getAuditStatus())){
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            //查询isdelete属性为空的所有
            criteria.andIsDeleteIsNull();
        }
        Page<Goods> pagelist=(Page<Goods>) goodsDao.selectByExample(query);
        return new PageResult(pagelist.getTotal(),pagelist.getResult());
    }

    /**
     * 商品管理修改数据回显
     * @param id
     * @return
     */
    @Override
    public GoodsEntity findOne(Long id) {
        //查找商品对象
        Goods goods = goodsDao.selectByPrimaryKey(id);
        //查找商品详情对象
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        //查找库存集合对象
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        GoodsEntity goodsEntity = new GoodsEntity();
        goodsEntity.setGoods(goods);
        goodsEntity.setGoodsDesc(goodsDesc);
        goodsEntity.setItemList(itemList);
        return goodsEntity;
    }

    /**
     * 商品管理数据修改数据保存
     * @param goodsEntity
     */
    @Override
    public void update(GoodsEntity goodsEntity) {
      //修改商品对象
        goodsDao.updateByPrimaryKeySelective(goodsEntity.getGoods());
      //修改商品描述对象
        goodsDescDao.updateByPrimaryKeySelective(goodsEntity.getGoodsDesc());
      //修改库存对象
        //根据商品id清空对应的库存数据
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(goodsEntity.getGoods().getId());
        itemDao.deleteByExample(query);
        //添加库存集合数据
        insert(goodsEntity);


    }

    /**
     * 批量删除
     * @param id
     */
    @Override
    public void delete(final Long id) {
        /**
         * 根据商品id 逻辑删除数据库对应商品
         */
              Goods goods = new Goods();
              goods.setId(id);
              goods.setIsDelete("1");
              goodsDao.updateByPrimaryKeySelective(goods);

        /**
         * 将商品id作为文本发送给消息服务器
         */
        jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                return textMessage;
            }
        });
    }

    /**
     * 根据商品的id修改商品的审核状态
     * @param id
     * @param status
     * @return
     */
    @Override
    public void updateStatus(final Long id, String status) {

        /**
         * 1.根据id到数据库中改变数据的上架状态
         */
        //根据商品id修改商品类的状态
        Goods goods = new Goods();
               goods.setId(id);
               goods.setAuditStatus(status);
               goodsDao.updateByPrimaryKeySelective(goods);

               //根据商品id修改库存集合对象的状态码
               Item item = new Item();
               item.setStatus(status);
               ItemQuery itemQuery = new ItemQuery();
               ItemQuery.Criteria criteria = itemQuery.createCriteria();
               criteria.andGoodsIdEqualTo(id);
               itemDao.updateByExampleSelective(item,itemQuery);

        /**
         * 2.把商品id传给消息服务器
         */
        jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                //将商品id传给消息服务器
                TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                return textMessage;
            }
        });

    }


    //封装设置item数据代码
    private Item setItemValue(GoodsEntity goodsEntity, Item item){
        //3-1设置item数据

        //设置创建时间
        item.setCreateTime(new Date());
        //设置更新时间
        item.setUpdateTime(new Date());
        //设置商品id 商品spu编号
        item.setGoodsId(goodsEntity.getGoods().getId());
        //设置分类id
        item.setCategoryid(goodsEntity.getGoods().getCategory3Id());
        //设置商家编号
        item.setSellerId(goodsEntity.getGoods().getSellerId());

        //设置品牌名称
        Brand brand = brandDao.selectByPrimaryKey(goodsEntity.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //设置分类名称
        ItemCat itemCat = itemCatDao.selectByPrimaryKey(goodsEntity.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());
        //设置商家名称
        Seller seller = sellerDao.selectByPrimaryKey(goodsEntity.getGoods().getSellerId());
        item.setSeller(seller.getName());
        //图片地址
        String itemImages = goodsEntity.getGoodsDesc().getItemImages();
        List<Map> maps = JSON.parseArray(itemImages, Map.class);
        if(maps!=null&&maps.size()>0){
            String url = String.valueOf(maps.get(0).get("url"));
            item.setImage(url);
        }
        //设置初始化状态
        item.setStatus("0");

        return item;
    }

    //封装保存集合数据方法
    public void insert(GoodsEntity goodsEntity){
        if("1".equals(goodsEntity.getGoods().getIsEnableSpec())){
            //勾选上了库存集合 有库存数据
            if(goodsEntity.getItemList()!=null){
                for (Item item : goodsEntity.getItemList()) {
                    //设置标题 由商品名称和规格组成 供消费者查询
                    String title = goodsEntity.getGoods().getGoodsName();
                    //取出json类型的数据
                    String spec = item.getSpec();
                    //将json类型的数据转换为java对象
                    Map<String,Object>specMap = JSON.parseObject(item.getSpec());
                    //获取specMap中的value集合
                    Collection<Object> values = specMap.values();
                    for (Object value : values) {
                        title+=value+""+title;
                    }
                    item.setTitle(title);

                    //设置item属性
                    setItemValue(goodsEntity,item);

                    itemDao.insertSelective(item);
                }
            }

        }else{
            //没有勾选上库存集合 没有库存数据  需要初始化一条数据
            Item item = new Item();
            //价格
            item.setPrice(new BigDecimal("99999999999"));
            //库存
            item.setNum(0);
            //初始化规格
            item.setSpec("{}");
            //标题
            item.setTitle(goodsEntity.getGoods().getGoodsName());
            //调用方法
            setItemValue(goodsEntity,item);
            //添加数据
            itemDao.insertSelective(item);
        }
    }
}
