package com.xyl.huala.wechat.v3.dao;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSON;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.BSellerAmount;
import com.xyl.huala.entity.BSellerAmountDetail;
import com.xyl.huala.entity.BSellerAmountLog;
import com.xyl.huala.entity.BSellerOrderBonus;
import com.xyl.huala.weixin.util.UUIDUtil;

@Repository
public class V3BalanceDao {
	public static final Logger log = LoggerFactory.getLogger(V3BalanceDao.class);
	@Autowired
	private JdbcDao jdbcDao;
	@Autowired
	private JdbcOperations jdbcTemplate;
	//@Transactional
	public void balanceExec(Long orderId){
		String createNo=UUIDUtil.getUUID().toUpperCase();
		createSellerOrderBonus(createNo,orderId);
		createBalance(createNo);
	}
	
	/**
	 * 生成订单明细的分成详情
	 * @param createNo
	 */
	public void createSellerOrderBonus(String createNo,Long orderId) {
		//
		List<BSellerOrderBonus> orderList = jdbcDao.queryByCode("banlance.getOrderById", BSellerOrderBonus.class,orderId,orderId);
		for(BSellerOrderBonus b:orderList){
			BSellerOrderBonus bo=new BSellerOrderBonus();
			BeanUtils.copyProperties(b, bo);
			bonus(bo);//优惠信息
			long selfIncome = bo.getSelfGoodsAmount()*bo.getSelfGoodsBonus();
			long marketIncome =( bo.getMarketGoodsSaleAmount()-bo.getMarketGoodsRecAmount())*bo.getMarketGoodsBonus();
			marketIncome=marketIncome<0?0L:marketIncome;
			bo.setWithdrawAmount((selfIncome+marketIncome)/10000+bo.getShippingAmount()-bo.getSelfCouponAmount()-bo.getSelfShippingCouponAmount());
			if(bo.getWithdrawAmount()<0){
				bo.setRemark("店铺收益为"+bo.getWithdrawAmount()+"修正为0------");
				bo.setWithdrawAmount(0L);
			}
			bo.setHualaAmount(bo.getPayAmount()-bo.getWithdrawAmount());
			bo.setRemark((bo.getRemark()!=null?"":bo.getRemark())+"自营商品*自营分成比利+（超市销售价-超市进价）*利润分成比+运费 -运费优惠-其他优惠 （注意：退货商品与红包不计算在内）");
			bo.setAddTime(new Date());
			bo.setCreateNo(createNo);
			jdbcDao.insert(bo);
			log.info(JSON.toJSONString(bo));
		}
	}
	/**
	 * 优惠信息处理
	 * @param bo
	 */
	private void bonus(BSellerOrderBonus bo){
		List<BSellerOrderBonus> orderBonus = jdbcDao.queryByCode("banlance.getOrderCoupn", BSellerOrderBonus.class,bo.getOrderId());
		BSellerOrderBonus b;
		if(orderBonus!=null&&orderBonus.size()>0){
			b=orderBonus.get(0);
			bo.setSelfShippingCouponAmount(b.getSelfShippingCouponAmount());
			bo.setSelfCouponAmount(b.getSelfCouponAmount());
			bo.setHualaCouponAmount(b.getHualaCouponAmount());
			bo.setHualaShippingCouponAmount(b.getHualaShippingCouponAmount());
		}else{
			bo.setSelfShippingCouponAmount(0l);
			bo.setSelfCouponAmount(0l);
			bo.setHualaCouponAmount(0l);
			bo.setHualaShippingCouponAmount(0l);
		}
	}
	
	/**
	 * 生成结算单详情，并回写订单批次号
	 * @param bo
	 */
	private void createBalance(String createNo){
		List<BSellerAmountDetail> bList=jdbcDao.queryByCode("banlance.createAmount", BSellerAmountDetail.class,createNo);
		for(BSellerAmountDetail b:bList){
			b.setWithdraw("bank");
			jdbcDao.insert(b);
			BSellerAmount bs=new BSellerAmount();
			bs.setSellerId(b.getSellerId());
			//判断商户余额是否为空，不为空则将结算金额加入余额，如果为空，则插入记录
			bs=jdbcDao.queryOne(bs);
			if(bs!=null){
				bs.setTotalAmount(bs.getTotalAmount()+b.getAmount());
				jdbcDao.update(bs);
			}else{
				bs=new BSellerAmount();
				bs.setSellerId(b.getSellerId());
				bs.setPaysAmount(0l);
				bs.setUpdateTime(new Date());
				bs.setTotalAmount(b.getAmount());
				jdbcDao.insert(bs);
			}
			//更新订单批次号与分成明细表的订单明细批次号
			String orderBonus = "update b_seller_order_bonus a,h_order b set a.batch_no=?,b.batch_no=? where a.order_id=b.id and a.create_no=? and a.seller_id=?";
			jdbcTemplate.update(orderBonus, b.getBatchNo(),b.getBatchNo(),createNo,b.getSellerId());
			
			BSellerAmountLog bsl=new BSellerAmountLog();
			bsl.setAddTime(new Date());
			bsl.setAddUserId(0l);
			bsl.setNewValue(""+b.getAmount());
			bsl.setOldValue("无");
			bsl.setRemark("自动结算日志：----生成批次号（createNo）："+createNo+"---结算批次号（batch_no）:"+b.getBatchNo());
			bsl.setSellerId(b.getSellerId());
			jdbcDao.insert(bsl);
			
			
		}
	}
	
}
