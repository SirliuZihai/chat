package com.zihai.account.service;

import java.math.BigDecimal;

import org.bson.Document;

public interface AccountService {
	/**查询余额
	*/
	public Document getAccountInfo();
	/**
	 * 余额交易
	 * */
	public void trade(String credit,String debit,BigDecimal amt);
	/**
	 * 幸运草
	 * */
	public void four_leave_color();
}
