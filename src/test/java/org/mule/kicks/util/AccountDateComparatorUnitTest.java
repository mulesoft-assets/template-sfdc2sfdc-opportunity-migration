package org.mule.kicks.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mock;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.kicks.util.AccountDateComparator;

public class AccountDateComparatorUnitTest {

	private static final String QUERY_COMPANY_A = "usersFromOrgA";
	private static final String QUERY_COMPANY_B = "usersFromOrgB";

	@Mock
	private MuleContext muleContext;

	@Test
	public void testSyncAccountFromA() throws TransformerException {
		
		AccountDateComparator comparator = new AccountDateComparator();
		Assert.assertTrue("Account in A has newer LastReferenceDate and should be migrated", comparator.isAfter(getAccountInA(), getAccountInB()));
		
	}
	
	@Test
	public void testSyncAccountFromASinceAccountBWasNeverReferenced() throws TransformerException {
		
		AccountDateComparator comparator = new AccountDateComparator();
		Assert.assertTrue("Account in B has never been referenced, therefore LastReferenceDate is null and AccountA should be migrated", comparator.isAfter(getAccountInA(), getAccountInBNeverReferenced()));
		
	}
	
	@Test
	public void testSyncAccountFromB() throws TransformerException {
		
		AccountDateComparator comparator = new AccountDateComparator();
		Assert.assertFalse("Account in A has newer LastReferenceDate and should be migrated", comparator.isAfter(getAccountInA(), getAccountInBWithNewerDate()));
		
	}

	private Map<String, String> getAccountInA() {
	
		Map<String, String> accountInA = new HashMap<String, String>();
		accountInA.put("Id", "1");
		accountInA.put("LastModifiedDate", "2014-01-22T14:00:00.000Z");
		accountInA.put("Name", "FakeAccountInA");

		return accountInA;

	}
	
	private Map<String, String> getAccountInB() {
		
		Map<String, String> accountInA = new HashMap<String, String>();
		accountInA.put("Id", "2");
		accountInA.put("LastModifiedDate", "2014-01-22T11:00:00.000Z");
		accountInA.put("Name", "FakeAccountInB");

		return accountInA;

	}

	private Map<String, String> getAccountInBNeverReferenced() {
		
		Map<String, String> accountInA = new HashMap<String, String>();
		accountInA.put("Id", "2");
		accountInA.put("LastModifiedDate", null);
		accountInA.put("Name", "FakeAccountInB");

		return accountInA;

	}

	private Map<String, String> getAccountInBWithNewerDate() {
		
		Map<String, String> accountInA = new HashMap<String, String>();
		accountInA.put("Id", "2");
		accountInA.put("LastModifiedDate", "2014-01-22T21:00:00.000Z");
		accountInA.put("Name", "FakeAccountInB");

		return accountInA;

	}
	
}
