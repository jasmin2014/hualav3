/**
 * 
 */
package com.xyl.huala.sys.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;

import com.xyl.core.jdbc.interceptor.PageControl;
import com.xyl.core.jdbc.persistence.JdbcDao;
import com.xyl.huala.entity.TSql;

/**
 * @author leazx
 *
 */
@Repository
@Profile("mysql")
@SuppressWarnings("rawtypes")
public class ReportDao {
	@Autowired
	private JdbcOperations jdbcTemplate;
	@Autowired
	private JdbcDao jdbcDao;

	/**
	 * 获取sql中数据的列表
	 * 
	 * @param groupKey
	 * @param sqlKey
	 * @param param
	 * @return
	 */
	public List getSqlList(Pageable page, String groupKey, String sqlKey, String... param) {
		TSql tsql = jdbcDao.queryOne(new TSql(groupKey, sqlKey));
		if (page != null) {
			PageControl.performPage(page.getPageNumber(), page.getPageSize());
		}
		return jdbcTemplate.queryForList(tsql.getSqlText(), param);
	}

	/**
	 * 获取 sql中单条数据集
	 * 
	 * @param groupKey
	 * @param sqlKey
	 * @param date
	 * @return
	 */
	public Map<String, Object> getSqlOne(String groupKey, String sqlKey, String... param) {
		TSql tsql = jdbcDao.queryOne(new TSql(groupKey, sqlKey));
		return jdbcTemplate.queryForMap(tsql.getSqlText(), param);
	}
}
