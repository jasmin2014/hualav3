package com.xyl.huala.wechat.v3.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.xyl.huala.core.annotation.Base64;
import com.xyl.huala.wechat.v3.config.Authentication;
import com.xyl.huala.wechat.v3.domain.CartInfo;
import com.xyl.huala.wechat.v3.domain.CartItem;
import com.xyl.huala.wechat.v3.domain.DataRet;
import com.xyl.huala.wechat.v3.domain.Order;
import com.xyl.huala.wechat.v3.domain.Seller;
import com.xyl.huala.wechat.v3.domain.SellerGoods;
import com.xyl.huala.wechat.v3.service.V3CartService;
import com.xyl.huala.wechat.v3.service.V3OrderService;
import com.xyl.huala.wechat.v3.service.V3SellerService;
import com.xyl.huala.wechat.v3.util.WxSession;

import jxl.common.Logger;

@RestController
@RequestMapping("v3")
public class V3OrderWeb {
	
	private Logger logger = Logger.getLogger(V3OrderWeb.class);
	
    @Autowired
    private V3OrderService orderService;
    @Autowired
    private V3SellerService sellerService;
    @Autowired
    private V3CartService cartService;
    /*******************************************
     * ***********购物车管理**********************
     ******************************************/
    /**
     * 增加购物车
     *
     * @param cart
     * @return
     */
    @RequestMapping(value = "cart/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public DataRet<CartInfo> add(@RequestBody CartInfo cart, HttpSession session) {
        DataRet<CartInfo> ret = new DataRet<CartInfo>();
        Long userId = WxSession.getUserId();
        if (userId != null) {
            cart.setUserId(userId.toString());
        } else {
            cart.setUserId(session.getId());
        }
        cart = cartService.add(cart);
        ret.setBody(cart);
        return ret;
    }

    /**
     * 减少购物车
     *
     * @param cart
     * @return
     */
    @RequestMapping(value = "cart/reduce", produces = MediaType.APPLICATION_JSON_VALUE)
    public DataRet<CartInfo> reduce(@RequestBody CartInfo cart,
                                    HttpSession session) {
        DataRet<CartInfo> ret = new DataRet<CartInfo>();
        Long userId = WxSession.getUserId();
        if (userId != null) {
            cart.setUserId(userId.toString());
        } else {
            cart.setUserId(session.getId());
        }
        cart = cartService.reduce(cart);
        ret.setBody(cart);
        return ret;
    }

    /**
     * 購物车列表
     *
     * @return
     */
    @RequestMapping(value = "cart/list")
    public DataRet<List<CartInfo>> getCartList(HttpSession session) {
        DataRet<List<CartInfo>> ret = new DataRet<List<CartInfo>>();
        Long userId = WxSession.getUserId();
        String user = null;
        if (userId != null) {
            user = userId.toString();
            cartService.mergeCart(session.getId(), user);
        } else {
            user = session.getId();
        }
        List<CartInfo> cartList = cartService.getList(user);
        ret.setBody(cartList);
        return ret;
    }

    /**
     * 选中购物车接口
     *
     * @return
     */
    @RequestMapping(value = "cart/choose/{sellerId}")
    public DataRet<Boolean> chooseCart(@PathVariable Long sellerId, Long skuId,
                                       Boolean choose, HttpSession session) {
        DataRet<Boolean> ret = new DataRet<Boolean>();
        Long userId = WxSession.getUserId();

        Boolean b = cartService.chooseCart(sellerId, skuId,
                userId == null ? session.getId() : userId.toString(), choose);
        if (b) {
            ret.setBody(choose);
        } else {
            ret.setErrorCode("no data change");
            ret.setMessage("没有数据被修改");
        }
        return ret;
    }

    /**
     * 購物車按照店铺分组显示，购物车页面,如果已登录，则先合并在获取
     * @param session
     * @return
	 */
    @RequestMapping(value = "cart/seller-list")
    public DataRet<List<CartItem>> getCartSellerList(HttpSession session) {
        DataRet<List<CartItem>> ret = new DataRet<List<CartItem>>();
        List<CartItem> cartGroup = new ArrayList<>();
        Long userId = WxSession.getUserId();
        String user = null;
        if (userId != null) {
            user = userId.toString();
            cartService.mergeCart(session.getId(), user);
        } else {
            user = session.getId();
        }
        List<CartInfo> cartList = cartService.getList(user);
        for (CartInfo cart : cartList) {// 购物车分组处理
            try {
                SellerGoods sg = sellerService.getGoodsDetail(cart.getSkuId(),   cart.getSkuId());//SKUID其实是goodsid
                cart.setPicUrl(sg.getPicUrl());
                cart.setSalePrice(sg.getPrice());
                cart.setSupplierId(sg.getSupplierId());
                cart.setGoodsName(sg.getTitle());
                cart.setGoodsId(sg.getGoodsId());
                cart.setSupplierName(sg.getSupplierName());
                // 分組标记位，如果已加入到分组，则不需要处理修改为true;
                CartItem cg = null;
                for (CartItem c : cartGroup) {
                    if (c.getSellerId().longValue() == cart.getSellerId()
                            .longValue()) {
                        cg = c;
                        break;
                    }
                }
                if (cg == null) {
                    Seller seller = sellerService.getSellerById(cart
                            .getSellerId());
                    cg = new CartItem();
                    cg.setStartAmount(seller.getStartAmount() == 0 ? 0L
                            : seller.getStartAmount());
                    cg.setUserId(cart.getUserId());
                    cg.setSellerId(cart.getSellerId());
                    cg.setName(seller.getName());
                    cg.setSellerStatus(seller.getSellerStatus());
                    cg.addCart(cart);
                    if (!cart.getChoose()) {
                        cg.setChooseAll(false);
                    } else {
                        cg.setTotal(cg.getTotal() + cart.getSalePrice()
                                * cart.getGoodNum());
                    }
                    cartGroup.add(cg);
                } else {
                    if (!cart.getChoose()) {
                        cg.setChooseAll(false);
                    } else {
                        cg.setTotal(cg.getTotal() + cart.getSalePrice()
                                * cart.getGoodNum());
                    }
                    cg.addCart(cart);
                }
            } catch (RuntimeException e) {
            	logger.error("购物车数据错误！",e);
                cartService.delete(cart);
            }
        }
        ret.setBody(cartGroup);
        return ret;
    }

    /*******************************************************************
     *                 订单处理
     ********************************************************************/
    /**
     * 我的订单列表页面
     * @param isHistory
     * @return
	 */
    @RequestMapping(value = "order/list", method = RequestMethod.GET)
    @Authentication
    public DataRet<List<Order>> orderList(String isHistory) {
        DataRet<List<Order>> ret = new DataRet<List<Order>>();
        Long userId = WxSession.getUserId();// WxSession.getUserId();
        List<Order> orderinfo = orderService.getOrderList(userId, Boolean.parseBoolean(isHistory));

        ret.setBody(orderinfo);
        return ret;
    }

    /**
     * 订单详情
     *
     * @param orderId
     * @return
     */
    @RequestMapping(value = "order/view/{orderId}", method = RequestMethod.GET)
    @Authentication
    public DataRet<?> orderDetail(@PathVariable("orderId") Long orderId) {
        DataRet<Map<String, Object>> ret = new DataRet<Map<String, Object>>();
        Order orderInfo = orderService.getOrderInfo(orderId);
        Seller seller = sellerService.getSellerById(Long.parseLong(orderInfo.getReferer()));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("order", orderInfo);
        map.put("seller", seller);
        ret.setBody(map);
        return ret;
    }

    /**
     * 确认下单数据接口
     * @param sellerId
     * @return
	 */
    @RequestMapping(value = "order/confirm/{sellerId}", method = RequestMethod.GET)
    @Authentication
    public DataRet<Order> orderConfirm(@PathVariable("sellerId") Long sellerId) {
        DataRet<Order> ret = new DataRet<Order>();
        Long userId = WxSession.getUserId();
        Order orderConfirm = orderService.getOrderConfirm(userId, sellerId);
        ret.setBody(orderConfirm);
        return ret;
    }

    /**
     * 历史订单列表（我的订单的代码片断）
     * @param sellerId
     * @return
	 */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @RequestMapping(value = "order/choosedate/{sellerId}", method = RequestMethod.GET)
    @ResponseBody
    public DataRet chooseDate(@PathVariable("sellerId") Long sellerId) {
        DataRet ret = new DataRet<Order>();
        ret.setBody(orderService.chooseDate(sellerId));
        return ret;
    }

    /**
     * 订单确认：检验订单数据的正确性，然后保存订单数据
     *
     * @param sellerId
     * @param order
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    @RequestMapping(value = "order/confirm/{sellerId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @Authentication
    public DataRet orderConfirm(@PathVariable("sellerId") Long sellerId, @Base64 Order order) {
        DataRet ret = new DataRet();
        Long userId = WxSession.getUserId();
        order.setUserId(userId);
        ret = orderService.checkOrder(sellerId, order);
        if (ret.isSuccess()) {
            cartService.clearCart(userId, sellerId);
        }
        return ret;
    }

    /**
     * 订单处理：cancel取消订单：confirm：确认收货
     */
    @RequestMapping(value = "order/orderStatus", method = RequestMethod.GET)
    @Authentication
    public DataRet<String> orderExec(Long orderId, String orderStatus) {
        DataRet<String> ret = new DataRet<String>();
        orderService.orderExec(orderId, orderStatus);
        return ret;
    }
}
