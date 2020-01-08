package com.zihai.account.service;

import java.math.BigDecimal;

import org.bson.Document;

import com.zihai.util.MongoUtil;

public class AccountServiceImp implements AccountService {

	@Override
	public Document getAccountInfo() {
		//MongoUtil.Query(new Document("username",""), projection, clazz, collectionName)
		return null;
	}

	@Override
	public void trade(String credit, String debit, BigDecimal amt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void four_leave_color() {
		// TODO Auto-generated method stub

	}

}
