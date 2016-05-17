/**
 * @Probject Name: pcm-service
 * @Path: com.wangfj.utilCompareDate.java
 * @Create By duanzhaole
 * @Create In 2015年7月21日 下午5:51:05
 * TODO
 */
package com.wangfj.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Class Name CompareDate
 * @Author duanzhaole
 * @Create In 2015年7月21日
 */
public class CompareDate {

	public static int compare_date(String DATE1, String DATE2) {

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		try {
			Date dt1 = df.parse(DATE1);
			Date dt2 = df.parse(DATE2);
			if (dt1.getTime() > dt2.getTime()) {
				System.out.println("dt1 在dt2前");
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				System.out.println("dt1在dt2后");
				return 2;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}
	
	
	 public static String getDate() {
		 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		 return formatter.format(new Date());
	}
}
