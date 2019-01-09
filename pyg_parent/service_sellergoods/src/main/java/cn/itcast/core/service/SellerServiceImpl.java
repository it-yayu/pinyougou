package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {
    @Autowired
    SellerDao sellerDao;
    //添加注册
    @Override
    public void add(Seller seller) {
        //设置注册时间
        seller.setCreateTime(new Date());
        //设置审核状态默认是0 不通过审核
        seller.setStatus("0");
        sellerDao.insertSelective(seller);
    }

    /**
     * 商家审核高级分页模糊查询
     * @param seller
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult search(Seller seller, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        SellerQuery query = new SellerQuery();
        SellerQuery.Criteria criteria = query.createCriteria();
        if(seller!=null){
            if(seller.getStatus()!=null&&!"".equals(seller.getStatus())){
               criteria.andStatusEqualTo(seller.getStatus());
            }
            if(seller.getName()!=null&&!"".equals(seller.getName())){
                criteria.andNameLike("%"+seller.getName()+"%");
            }
            if(seller.getNickName()!=null&&!"".equals(seller.getNickName())){
                criteria.andNameLike("%"+seller.getNickName()+"%");
            }
        }
        Page<Seller> pageList=(Page<Seller>)sellerDao.selectByExample(query);
        return new PageResult(pageList.getTotal(),pageList.getResult());
    }

    /**
     * 展示详情
     * @param id
     * @return
     */
    @Override
    public Seller findOne(String id) {
        return sellerDao.selectByPrimaryKey(id);
    }

    /**
     * 更改状态
     * @param sellerId
     * @param status
     */
    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
