package com.xyl.huala.wechat.v3.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.xyl.huala.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalanceUtils {
	private static Logger log = LoggerFactory.getLogger(BalanceUtils.class);

	public class ChooseDate {
		private String day;
		private String showTime;
		private Date bestTime;

		public String getDay() {
			return day;
		}

		public void setDay(String day) {
			this.day = day;
		}

		public Date getBestTime() {
			return bestTime;
		}

		public void setBestTime(Date bestTime) {
			this.bestTime = bestTime;
		}

		public String getShowTime() {
			return showTime;
		}

		public void setShowTime(String showTime) {
			this.showTime = showTime;
		}

	}

	/**
	 * 
	 * @param open
	 * @param close
	 * @return
	 */
	public List getTodayList(Date open, Date close) {
		Date now = new Date();
		now=DateUtils.getDateFromStr(DateUtils.getNowDateStr()+" "+now.getHours()+":00:00");
		String date=DateUtils.getNowDateStr();
		open=DateUtils.getDateTime(date+" "+open.getHours()+":"+open.getMinutes(), "yyyy-MM-dd HH:mm");
		close=DateUtils.getDateTime(date+" "+close.getHours()+":"+close.getMinutes(), "yyyy-MM-dd HH:mm");
		List today = new ArrayList<>();
		Date temp = DateUtils.addHours(now, 2);
		if (temp.before(open)) {
			now = open;
		}
		now = DateUtils.addHours(now, 1);
		while (now.before(close)) {
			ChooseDate c = new ChooseDate();
			c.setDay("今天");
			c.setBestTime(DateUtils.addHours(now, 2));
			c.setShowTime(DateUtils.getDateTime(now, "HH:mm") + "-"
					+ DateUtils.getDateTime(DateUtils.addHours(now, 2), "HH:mm"));
			now = DateUtils.addHours(now, 2);
			today.add(c);
		}

		return today;
	}
	/**
	 * 
	 * @param open
	 * @param close
	 * @return
	 */
	public List getTomorrowList(Date open, Date close) {
		List today = new ArrayList<>();
		String date=DateUtils.getNowDateStr();
		open=DateUtils.getDateTime(date+" "+open.getHours()+":"+open.getMinutes(), "yyyy-MM-dd HH:mm");
		close=DateUtils.getDateTime(date+" "+close.getHours()+":"+close.getMinutes(), "yyyy-MM-dd HH:mm");
		Date now = open;
		while (now.before(close)) {
			ChooseDate c = new ChooseDate();
			c.setDay("明天");
			c.setBestTime(DateUtils.addHours(now, 2));
			c.setShowTime(DateUtils.getDateTime(now, "HH:mm") + "-"
					+ DateUtils.getDateTime(DateUtils.addHours(now, 2), "HH:mm"));
			now = DateUtils.addHours(now, 2);
			today.add(c);
		}
		return today;
	}
}
