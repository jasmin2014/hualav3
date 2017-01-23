/**
 * 
 */
package com.xyl.huala.sys.service.mysql;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.xyl.huala.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.xyl.huala.sys.dao.ReportDao;
import com.xyl.huala.sys.service.ReportService;

/**
 * 
 * @author leazx
 *
 */
@Service
@Profile("mysql")
public class ReportServiceImpl implements ReportService {
	@Autowired
	private ReportDao reportDao;
	@Autowired
	private MongoTemplate mongo;

	/**
	 * 历史数据保存到mongo中，如果没有则保存，如果存在，则直接获取
	 */
	private static String everyDayReport = "Report-EveryDayReport";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map<?, ?> getEveryDayReport(String date) {
		Integer d = Integer.parseInt(date);
		Integer n = Integer.parseInt(DateUtils.date(new Date(), "yyyyMMdd"));
		Map m;
		if (d < n) {
			if (!mongo.collectionExists(everyDayReport)) {
				mongo.createCollection(everyDayReport);
			}
			m = mongo.findOne(Query.query(Criteria.where("date").is(date)), HashMap.class, everyDayReport);
			if (m == null) {
				m = new HashMap();
				Map<?, ?> orderInfo = reportDao.getSqlOne("everyreport", "everyOrder", date);
				Map<?, ?> sellerInfo = reportDao.getSqlOne("everyreport", "everySeller", date);
				Map<?, ?> userInfo = reportDao.getSqlOne("everyreport", "everyUser", date);
				m.putAll(orderInfo);
				m.putAll(sellerInfo);
				m.putAll(userInfo);
				m.put("_id", date);
				mongo.insert(m, everyDayReport);
			}
		} else {
			m = new HashMap();
			Map<?, ?> orderInfo = reportDao.getSqlOne("everyreport", "everyOrder", date);
			Map<?, ?> sellerInfo = reportDao.getSqlOne("everyreport", "everySeller", date);
			Map<?, ?> userInfo = reportDao.getSqlOne("everyreport", "everyUser", date);
			m.putAll(orderInfo);
			m.putAll(sellerInfo);
			m.putAll(userInfo);
		}
		return m;
	}

	/**
	 * 每日报表
	 */
	private static String everyDaySellerList = "Report-EveryDaySellerList";

	/**
	 * 每日报表查询
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List getEverySellerListDayReport(String date, Pageable page) {
		Integer d = Integer.parseInt(date);
		Integer n = Integer.parseInt(DateUtils.date(new Date(), "yyyyMMdd"));
		List<?> m;
		if (d < n) {
			m = mongo.find(
					Query.query(Criteria.where("date").is(date))
							.with(new PageRequest(page.getPageNumber() - 1, page.getPageSize())),
					HashMap.class, everyDaySellerList);
		} else {
			m = reportDao.getSqlList(page, "everyreport", "sellerListSeller", date);
			for (Object seller : m) {
				if (seller instanceof Map) {
					Map sel = (Map) seller;
					Object string = sel.get("seller_id");
					try {
						Map<?, ?> sellerInfo = reportDao.getSqlOne("everyreport", "sellerListOrder", date,
								string.toString());
						sel.putAll(sellerInfo);
					} catch (Exception e) {

					}
				}
			}
		}
		return m;
	}

	/**
	 * 每日报表信息存储到mongo <br>
	 * 每日店铺信息统计保存到数据库中，减轻数据库查询压力
	 * 
	 * @param date
	 * @param page
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int saveEverySellerListDayReport(String date, Pageable page) {
		List<?> m = reportDao.getSqlList(page, "everyreport", "sellerListSeller", date);
		for (Object seller : m) {
			if (seller instanceof Map) {
				Map sel = (Map) seller;
				Object sellerId = sel.get("seller_id");
				try {
					Map<?, ?> sellerInfo = reportDao.getSqlOne("everyreport", "sellerListOrder", date,
							sellerId.toString());
					sel.putAll(sellerInfo);
					sel.put("_id", sellerId + date);
					mongo.insert(sel, everyDaySellerList);
				} catch (Exception e) {

				}
			}
		}

		return m == null ? 0 : m.size();
	}
}
