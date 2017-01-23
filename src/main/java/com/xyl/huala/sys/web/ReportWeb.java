package com.xyl.huala.sys.web;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xyl.huala.sys.service.ReportService;
import com.xyl.huala.wechat.v3.domain.DataRet;

@RestController
public class ReportWeb {
	private static final Logger logger = Logger.getLogger(ReportWeb.class);

	@Autowired
	private ReportService reportService;

	/**
	 * 获取每日统计报表
	 * 
	 * @param date
	 * @return
	 */
	@RequestMapping("report/everyday-report")
	public Map getEveryDayReport(String date) {
		return reportService.getEveryDayReport(date);
	}

	@RequestMapping("report/everyday-seller-list-report")
	public List getEveryDaySellerListReport(String date,Integer page,Integer size) {
		return reportService.getEverySellerListDayReport(date, new PageRequest(page, size));
	}

	/**
	 * 每日报表信息存储到mongo
	 * 
	 * @param date
	 * @param page
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("report/save-everyday-seller-list-report")
	public DataRet<String> saveEverySellerListDayReport(String date) {
		int count = 0;
		int pageSize=30;
		int pageNumber=1;
		int ret=0;
		do {
			count=reportService.saveEverySellerListDayReport(date, new PageRequest(pageNumber++, pageSize));
			ret+=count;
		} while (count == pageSize);
		return new DataRet<String>("保存成功："+ret);
	}
}
