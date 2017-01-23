package com.xyl.huala.wechat.v3.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.CommandResult;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.service.mongo.DataSyncService;

/**
 * 购物车信息
 *
 * @author wyl0153
 */
@Controller
public class DataSyncWeb {
    @Autowired
    private DataSyncService dataSyncService;
    @Autowired
    private MongoTemplate mongo;

    /**
     * 同步店铺信息
     *
     * @return
     */
    @RequestMapping("v3/sync-seller")
    @ResponseBody
    public DataRet<String> sycnSeller(Long sellerId) {
        dataSyncService.insertSeller(sellerId);
        return new DataRet<String>("同步店铺信息成功");
    }

    /**
     * 同步店铺信息
     *
     * @return
     */
    @RequestMapping("v3/sync-goods")
    @ResponseBody
    public DataRet<String> sycnGoods(Long goodsId) {
        dataSyncService.syncGoods(goodsId);
        return new DataRet<String>("同步商品信息成功");
    }

    /**
     * 同步店铺信息
     *
     * @return
     */
    @RequestMapping("v3/sync-goods-cat")
    @ResponseBody
    public DataRet<String> sycnGoodsCat(Long sellerId) {
        dataSyncService.syncGoodsCat(sellerId);
        return new DataRet<String>("同步商品分类信息成功");
    }

    @RequestMapping("v3/mongo/sellerList")
    @ResponseBody
    public List<Seller> manageSeller(Integer page) {
        return mongo.find(Query.query(Criteria.where("_id").gt(0)).with(new PageRequest(page, 10)), Seller.class);
    }

    @RequestMapping("mongo-seller")
    public String manageSellerIndex(Integer page) {
        return "v3/manage/mongo-data";
    }

    /**
     * 为mongo指定的表建立索引
     *
     * @param collection
     * @param name
     * @return
     */
    @RequestMapping("v3/mongo/createIndex")
    @ResponseBody
    public DataRet<String> createIndex(String collection, String name) {
        mongo.getCollection(collection).createIndex(name);
        return null;
    }

    @RequestMapping("v3/mongo/delete")
    @ResponseBody
    public DataRet<String> delete(String collection, String key, String value) {
        mongo.remove(Query.query(Criteria.where(key).is(value)), collection);
        return null;
    }

    /**
     * 执行mongo命令
     *
     * @param command
     * @return
     */
    @RequestMapping("v3/mongo/command")
    @ResponseBody
    public DataRet<String> command(final String command) {
        CommandResult cr = mongo.getDb().command(command);
        return new DataRet<String>(command + "执行成功");
    }
}
