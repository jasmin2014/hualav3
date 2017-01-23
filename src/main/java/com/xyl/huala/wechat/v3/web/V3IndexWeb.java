package com.xyl.huala.wechat.v3.web;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.core.annotation.LoginUser;
import com.xyl.huala.entity.CmsContext;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.domain.SellerIncomeDetail;
import com.xyl.huala.wechat.v3.service.V3IndexService;
import com.xyl.huala.wechat.v3.service.V3SellerService;
import com.xyl.huala.weixin.util.ConfKit;
import com.xyl.huala.weixin.wxapi.WxToken;

@Controller
public class V3IndexWeb {
    private static final Logger logger = Logger.getLogger(V3IndexWeb.class);
    @Autowired
    private V3SellerService sellerService;
    @Autowired
    private JdbcDao jdbcDao;
    @Autowired
    private V3IndexService indexService;

    /**
     * SPA首页，单页面应用入口程序
     *
     * @return
     */
    @RequestMapping("v3")
    @LoginUser(redirect = false)
    public String index(
            @CookieValue(required = false, name = "token") String openid,
            @CookieValue(required = false, name = "gps") String gpss,
            HttpServletResponse response, HttpServletRequest request,
            Model model) {
        // gps(openid, gpss, response);

        try {
            logger.info(request.getRequestURL() + request.getQueryString());
            String url = request.getRequestURL().toString();
            if (request.getQueryString() != null) {
                url += "?" + request.getQueryString();
            }
            Map<String, String> map = WxToken.getSign(url, ConfKit.getAppid(),
                    ConfKit.getAppSecret());
            model.addAllAttributes(map);
            logger.info(JSONObject.toJSON(map));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "/v3/index";
    }

    /**
     * 获取配置信息
     *
     * @return
     */
    @RequestMapping("v3/config")
    @ResponseBody
    public Map<String, String> getConfig(HttpServletRequest request) {

        Map<String, String> map = new HashMap<String, String>();
        map.put("imgUrl", System.getProperty("server_cfg.imgServer"));
        logger.info(JSONObject.toJSON(map));
        try {
            logger.info(request.getRequestURL() + request.getQueryString());
            String url = request.getRequestURL().toString();
            if (request.getQueryString() != null) {
                url += "?" + request.getQueryString();
            }
            Map<String, String> map1 = WxToken.getSign(url, ConfKit.getAppid(),
                    ConfKit.getAppSecret());
            map.putAll(map1);
            logger.info(JSONObject.toJSON(map));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;

    }

    /**
     * 用户协议
     *
     * @param openid
     * @param gpss
     * @param response
     * @return
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping("v3/context/{key}")
    @ResponseBody
    public DataRet aggrement(@PathVariable String key) {
        DataRet ret = new DataRet();
        CmsContext cms = new CmsContext();
        cms.setKeywords(key);
        CmsContext a = jdbcDao.queryOne(cms);
        ret.setBody(a.getContent());
        return ret;
    }

    /**
     * 用户协议
     *
     * @param openid
     * @param gpss
     * @param response
     * @return
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping("v3/context/page/{key}")
    public String contextPage(@PathVariable String key, Model model) {
        DataRet ret = new DataRet();
        CmsContext cms = new CmsContext();
        cms.setKeywords(key);
        CmsContext a = jdbcDao.queryOne(cms);
        model.addAttribute("context", a.getContent());
        return "context";
    }

    /**
     * 生成店铺的场景二维码
     *
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    @RequestMapping("v3/qrcod-seller/{sellerId}")
    public void createSellerQrcord(@PathVariable String sellerId, HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, IOException {
        StringBuffer requestURL = request.getRequestURL();
        int contextIndex = requestURL.indexOf(request.getContextPath());
        String host = requestURL.substring(0, contextIndex + request.getContextPath().length());
        BufferedImage img = indexService.getWxQcord(sellerId, host);
        ImageIO.write(img, "png", response.getOutputStream());
    }

    /**
     * 生成店铺跳转地址二维码
     * @param sellerId
     * @param request
     * @param response
     */
    @RequestMapping("v3/qrcod-seller-address/{sellerId}")
    public void createSellerAddressQrcod(@PathVariable String sellerId,HttpServletRequest request,HttpServletResponse response) throws IOException {
        StringBuffer requestURL = request.getRequestURL();
        int contextIndex = requestURL.indexOf(request.getContextPath());
        String host = requestURL.substring(0, contextIndex + request.getContextPath().length());
        BufferedImage img = indexService.getSellerWxAddressQcord(sellerId,host);
        ImageIO.write(img,"png",response.getOutputStream());
    }


    /**
     * 商户订单收益明细查询
     *
     * @param sellerId
     * @param request
     * @param response
     * @throws MalformedURLException
     * @throws IOException
     */
    @RequestMapping("v3/seller-income-detail/{sellerId}")
    public String sellerMonoy(@PathVariable String sellerId, Model model) {
        List<SellerIncomeDetail> queryByCode = jdbcDao.queryByCode("other.getSellerAmountList", SellerIncomeDetail.class, sellerId, sellerId);
        model.addAttribute("income", queryByCode);
        return "v3/manage/selller-income-detail";
    }
}
