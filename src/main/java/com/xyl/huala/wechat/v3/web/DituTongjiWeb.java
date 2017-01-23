package com.xyl.huala.wechat.v3.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.core.redis.RedisDao;
import com.xyl.huala.entity.HOrder;
import com.xyl.huala.entity.HSellerExtends;
import com.xyl.huala.entity.TSql;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.service.mongo.DataSyncService;

/**
 * 一些统计信息
 */
@Controller
@RequestMapping("tongji")
public class DituTongjiWeb {
    @Autowired
    private DataSyncService dataSyncService;
    @Autowired
    private MongoTemplate mongo;
    @Autowired
    private JdbcDao jdbcDao;
    @Autowired
    private JdbcOperations jdbcTemplate;
    @SuppressWarnings("rawtypes")
    @Autowired
    private RedisDao redisDao;

    /**
     * 同步店铺信息
     *
     * @return
     */
    @RequestMapping("sync-order")
    @ResponseBody
    public DataRet<String> sycnSeller(Long sellerId) {
        String sql = "select * from h_order where order_status='have_done'";
        List<HOrder> order = jdbcDao.queryBySQL(sql, HOrder.class);
        for (HOrder o : order) {
            mongo.insert(o);
        }
        return new DataRet<String>("同步店铺信息成功");
    }

    /**
     * 执行配置数据中的构思语句
     *
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @RequestMapping("sqlExe")
    @ResponseBody
    public List ditui(String key, String param, String isDownload) {
        String sql = "select * from t_sql a where CONCAT(a.group_key,'.',a.sql_key)=?";
        TSql tsql = jdbcDao.queryOneBySQL(sql, TSql.class, key);
        if (tsql.getParam() == null) {
            return jdbcTemplate.queryForList(tsql.getSqlText());
        }
        return jdbcTemplate.queryForList(tsql.getSqlText(), param);
    }

    /**
     * 查询统计报表sql语句
     *
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @RequestMapping("sql")
    @ResponseBody
    public List sql() {
        String sql = "select * from t_sql where  type='1'";
        return jdbcTemplate.queryForList(sql);
    }

    @RequestMapping("sellerext")
    @ResponseBody
    public List ditui(String sellerId) {
        String sql = "select * from h_seller_extends where seller_id=?";
        return jdbcDao.queryBySQL(sql, HSellerExtends.class, sellerId);
    }

}
