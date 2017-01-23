package com.xyl.huala.wechat.v3.service.mongo;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.WriteResult;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.core.redis.RedisDao;
import com.xyl.huala.wechat.v3.domain.CartInfo;
import com.xyl.huala.wechat.v3.service.V3CartService;

@Service
@Profile("mongo")
public class V3CartServiceImpl implements V3CartService {
	@Autowired
	private JdbcDao jdbcDao;
	@Autowired
	private RedisDao redisDao;
	@Autowired
	private MongoTemplate mongo;

	/**
	 * 增加购物车
	 * 
	 * @param userKey
	 *            sessionId或者userId
	 * @param sid
	 *            商铺信息
	 * @param num
	 *            商品数量
	 * @param goodSkuId
	 * @param type
	 *            0 为未登陆
	 * @return
	 */
	public CartInfo add(CartInfo cart) {
		Criteria where = Criteria.where("sellerId").is(cart.getSellerId());
		where.and("skuId").is(cart.getSkuId());
		where.and("userId").is(cart.getUserId());
		Query query = Query.query(where);
		CartInfo c = mongo.findOne(query, CartInfo.class);
		if (c == null) {
			cart.setGoodNum(1);
			c = cart;
			mongo.insert(c);
		} else {
			c.setGoodNum(c.getGoodNum() + 1);
			mongo.updateFirst(query, Update.update("goodNum", c.getGoodNum()).set("addTime", new Date()),
					CartInfo.class);
		}
		return c;
	}

	/**
	 * 减少购物车
	 * 
	 * @param userKey
	 *            sessionId或者userId
	 * @param sid
	 *            商铺信息
	 * @param num
	 *            商品数量
	 * @param goodSkuId
	 * @param type
	 *            0 为未登陆
	 * @return
	 */
	public CartInfo reduce(CartInfo cart) {
		Criteria where = Criteria.where("sellerId").is(cart.getSellerId());
		where.and("skuId").is(cart.getSkuId());
		where.and("userId").is(cart.getUserId());
		Query query = Query.query(where);
		CartInfo c = mongo.findOne(query, CartInfo.class);
		if (c != null && c.getGoodNum() <= 1) {
			c.setGoodNum(c.getGoodNum() - 1);
			mongo.remove(query, CartInfo.class);
		} else {
			c.setGoodNum(c.getGoodNum() - 1);
			mongo.updateFirst(query, Update.update("goodNum", c.getGoodNum()).set("addTime", new Date()),
					CartInfo.class);
		}
		return c;
	}

	/**
	 * 获取购物车信息
	 * 
	 * @param sellerId
	 * @param userId
	 * @param skuId
	 * @return
	 */
	public CartInfo getCart(Long sellerId, String userId, Long skuId) {
		Query query = Query.query(Criteria.where("sellerId").is(sellerId)
				.and("skuId").is(skuId).and("userId").is(userId));
		return mongo.findOne(query, CartInfo.class);
	}

	/**
	 * 取得购物车列表
	 * 
	 * @param userId
	 * @return
	 */
	@SuppressWarnings({})
	public List<CartInfo> getList(String userId) {
		Criteria where = Criteria.where("userId").is(userId);
		Query query = Query.query(where);
		List<CartInfo> cartList = mongo.find(query, CartInfo.class);
		return cartList;
	}

	/**
	 * 通过店铺ID与用户ID取得购物车列表
	 * 
	 * @param userId
	 *            用户ID
	 * @param sellerId
	 *            店铺ID
	 * @return
	 */
	@SuppressWarnings({})
	public List<CartInfo> getList(Long userId, Long sellerId) {
		Criteria where = Criteria.where("userId").is(userId).and("sellerId")
				.is(sellerId);
		Query query = Query.query(where);
		List<CartInfo> cartList = mongo.find(query, CartInfo.class);
		return cartList;
	}

	/**
	 * 更新购物车信息
	 * 
	 * @param userId
	 * @return
	 */
	@SuppressWarnings({})
	private void updateCart(CartInfo cart, Update update) {
		Query query = Query.query(Criteria.where("sellerId")
				.is(cart.getSellerId()).and("skuId").is(cart.getSkuId())
				.and("userId").is(cart.getUserId()));
		mongo.updateFirst(query, update, CartInfo.class);
	}

	/**
	 * 合并购物车，获取列表时，将购物车合并 <br>
	 * 更新购物车,将会话中的购物车信息更换成用户的ID，如果用户信息中没有，则直接修改userId，如果有则直接修改数量
	 * 
	 * @param sessionId
	 * @param userId
	 * @return
	 */
	@SuppressWarnings({})
	public void mergeCart(String sessionId, String userId) {
		if (userId == null) {
			return;
		}
		List<CartInfo> sessionCart = this.getList(sessionId);
		if (sessionCart != null && sessionCart.size() > 0) {
			for (CartInfo cart : sessionCart) {
				CartInfo c = this.getCart(cart.getSellerId(), userId,
						cart.getSkuId());
				Update update = null;
				if (c == null) {
					// 将会话中购物车更新为用户购物车
					update = Update.update("userId", userId);
					this.updateCart(cart, update);
				} else {
					// 将用户中购物车数据更新为会话中的数量
					update = Update.update("goodNum", cart.getGoodNum());
					this.updateCart(c, update);
				}
			}
		}
	}

	/**
	 * 选中购物车
	 * 
	 * @param sellerId
	 *            店铺ID
	 * @param skuId
	 *            如果商品信息为空，则是全选或或全部取消
	 * @param userId
	 *            用户ID ，如果没登录则是会话ID
	 */
	public Boolean chooseCart(Long sellerId, Long skuId, String userId,
			Boolean choose) {
		CartInfo c = this.getCart(sellerId, userId, skuId);
		Update update = Update.update("choose", choose);
		Criteria criteria = Criteria.where("sellerId").is(sellerId);
		if (skuId != null)
			criteria.and("skuId").is(skuId);
		criteria.and("userId").is(userId);
		Query query = Query.query(criteria);
		WriteResult rw = mongo.updateMulti(query, update, CartInfo.class);
		return rw.getN() > 0;
	}

	/**
	 * 清理购物车信息
	 * 
	 * @param userId
	 * @param sellerId
	 */
	public void clearCart(Long userId, Long sellerId) {
		Criteria where = Criteria.where("userId").is(""+userId).and("sellerId")
				.is(sellerId).and("choose").is(true);
		Query query = Query.query(where);
		mongo.remove(query, CartInfo.class);
	}

	@Override
	public void delete(CartInfo cart) {
		// TODO Auto-generated method stub
		
	}
}
