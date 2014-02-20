package org.mule.kicks.util;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mock;
import org.mule.api.MuleContext;
import org.mule.api.transformer.TransformerException;

public class OpportunityDateComparatorUnitTest {

	@Mock
	private MuleContext muleContext;

	@Test
	public void testSyncAccountFromA() throws TransformerException {
		assertTrue("Account in A has newer LastReferenceDate and should be migrated", OpportunityDateComparator.isAfter(getOpportunityInA(), getOpportunityInB()));
	}

	@Test
	public void testSyncAccountFromASinceAccountBWasNeverReferenced() throws TransformerException {
		assertTrue("Account in B has never been referenced, therefore LastReferenceDate is null and AccountA should be migrated",
				OpportunityDateComparator.isAfter(getOpportunityInA(), getOpportunityInBNeverReferenced()));
	}

	@Test
	public void testSyncAccountFromB() throws TransformerException {
		assertFalse("Account in A has newer LastReferenceDate and should be migrated", OpportunityDateComparator.isAfter(getOpportunityInA(), getOpportunityInBWithNewerDate()));
	}

	private Map<String, String> getOpportunityInA() {

		Map<String, String> opportunityInA = new HashMap<String, String>();
		opportunityInA.put("Id", "1");
		opportunityInA.put("LastModifiedDate", "2014-01-22T14:00:00.000Z");
		opportunityInA.put("Name", "FakeOpportunityInA");

		return opportunityInA;

	}

	private Map<String, String> getOpportunityInB() {

		Map<String, String> opportunityInA = new HashMap<String, String>();
		opportunityInA.put("Id", "2");
		opportunityInA.put("LastModifiedDate", "2014-01-22T11:00:00.000Z");
		opportunityInA.put("Name", "FakeOpportunityInB");

		return opportunityInA;

	}

	private Map<String, String> getOpportunityInBNeverReferenced() {

		Map<String, String> opportunityInA = new HashMap<String, String>();
		opportunityInA.put("Id", "2");
		opportunityInA.put("LastModifiedDate", null);
		opportunityInA.put("Name", "FakeOpportunityInB");

		return opportunityInA;

	}

	private Map<String, String> getOpportunityInBWithNewerDate() {

		Map<String, String> opportunityInA = new HashMap<String, String>();
		opportunityInA.put("Id", "2");
		opportunityInA.put("LastModifiedDate", "2014-01-22T21:00:00.000Z");
		opportunityInA.put("Name", "FakeOpportunityInB");

		return opportunityInA;

	}

}
