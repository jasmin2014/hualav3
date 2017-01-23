package com.xyl.huala.sys.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
/**
 * 报表统计相关内容
 * @author leazx
 *
 */
public interface ReportService {

	/**
	 * 获取每日统计报表
	 * <br>每家店铺每天生成前一天的统计信息
	 * @param date 日期，20160516
	 * @return
	 */
	public Map<?,?> getEveryDayReport(String date);
	
	
	/**
	 * 获取每日统计报表
	 * <br>每家店铺每天生成前一天的统计信息
	 * @param date 日期，20160516
	 * @return
	 */
	public List<Map<?,?>> getEverySellerListDayReport(String date, Pageable page);


	/**
	 * 每日报表信息存储到mongo
	 * @param date
	 * @param page
	 * @return
	 */
	public int saveEverySellerListDayReport(String date, Pageable page);
}
