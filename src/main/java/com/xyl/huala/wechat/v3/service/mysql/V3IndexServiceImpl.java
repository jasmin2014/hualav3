package com.xyl.huala.wechat.v3.service.mysql;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import com.mongodb.BasicDBObject;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.HSeller;
import com.xyl.huala.wechat.v3.service.V3IndexService;
import com.xyl.huala.weixin.util.Pic;
import com.xyl.huala.weixin.wxapi.Qrcod;

/**
 * v3版本首页相关接口
 *
 * @author leazx
 */
@Service
public class V3IndexServiceImpl implements V3IndexService {
    private static final String WX_QCORD = "wxQcord";//微信场景二维码字段名称
    @Autowired
    private JdbcDao jdbcDao;
    @Autowired
    private MongoTemplate mongo;

    /**
     * 生成维信场景二维码图片
     *
     * @param sellerId
     * @param request
     * @return
     */
    @Override
    public BufferedImage getWxQcord(String sellerId, String url) {
        BufferedImage img = null;

        Query query = Query.query(Criteria.where("sellerId").is(sellerId));
        BasicDBObject b = mongo.findOne(query, BasicDBObject.class, "wxQrcodImage");
        if (b == null) {//首先从mongo中获取图片的base63，如果取不到则从微信接口获取
            img = Pic.base64Decode(b.getString(WX_QCORD));
        } else {
            HSeller seller = jdbcDao.get(HSeller.class, Long.parseLong(sellerId));
            String qrcod = Qrcod.getQrcodUrl("hl_seller_" + sellerId);
            img = Pic.createPicTwo2(qrcod, url + "/assets/v3/images/huala-logo.jpg");
            img = Pic.addWord(img, seller.getName());
            BasicDBObject parameter = new BasicDBObject();
            parameter.put("sellerId", sellerId);
            parameter.put(WX_QCORD, Pic.base64Encode(img));
            mongo.save(parameter, "wxQrcodImage");
        }
        return img;
    }

    @Override
    public BufferedImage getSellerWxAddressQcord(String sellerId, String Url) throws IOException {
        BufferedImage img = null;
        Query query = Query.query(Criteria.where("sellerId").is(sellerId));
        BasicDBObject b = mongo.findOne(query,BasicDBObject.class,"wxQrcodImage");
        if(b!=null){
            img = Pic.base64Decode(b.getString(WX_QCORD));
        }else{
            HSeller seller = jdbcDao.get(HSeller.class,Long.parseLong(sellerId));
            //生成二维码地址
            img = Pic.createImage(Url+"/goshop/"+sellerId);
            //插入logo
            img= Pic.createPicTwo2(img, Url + "/assets/v3/images/huala-logo.jpg");
            img = Pic.addWord(img, seller.getName());
            BasicDBObject parameter = new BasicDBObject();
            parameter.put("sellerId", sellerId);
            parameter.put(WX_QCORD, Pic.base64Encode(img));
            mongo.save(parameter, "wxQrcodImage");

        }
        return img;
    }
}
