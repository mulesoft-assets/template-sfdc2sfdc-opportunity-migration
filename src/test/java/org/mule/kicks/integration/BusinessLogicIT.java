package org.mule.kicks.integration;

import static org.junit.Assert.assertEquals;
import static org.mule.kicks.builders.SfdcObjectBuilder.anAccount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.construct.Flow;
import org.mule.kicks.utils.BatchTestHelper;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.transport.NullPayload;

import com.sforce.soap.partner.SaveResult;

/**
 * The objective of this class is to validate the correct behavior of the Mule Kick that make calls to external systems.
 * 
 * The test will invoke the batch process and afterwards check that the accounts had been correctly created and that the ones that should be filtered are not in
 * the destination sand box.
 * 
 */
public class BusinessLogicIT extends AbstractKickTestCase {

	protected static final int TIMEOUT_SECONDS = 60;

	private static final String KICK_NAME = "accountmigration";

	private BatchTestHelper helper;

	private static SubflowInterceptingChainLifecycleWrapper checkAccountflow;
	private static List<Map<String, Object>> createdAccounts = new ArrayList<Map<String, Object>>();

	@Rule
	public DynamicPort port = new DynamicPort("http.port");

	@Before
	public void setUp() throws Exception {
		helper = new BatchTestHelper(muleContext);

		checkAccountflow = getSubFlow("retrieveAccountFlow");
		checkAccountflow.initialise();

		createTestDataInSandBox();
	}

	@After
	public void tearDown() throws Exception {
		deleteTestDataFromSandBox();
	}

	@Test
	public void testMainFlow() throws Exception {
		Flow flow = getFlow("mainFlow");
		flow.process(getTestEvent("", MessageExchangePattern.REQUEST_RESPONSE));

		helper.awaitJobTermination(TIMEOUT_SECONDS * 1000, 500);
		helper.assertJobWasSuccessful();

		assertEquals("The account should not have been sync", null, invokeRetrieveAccountFlow(checkAccountflow, createdAccounts.get(0)));

		assertEquals("The account should not have been sync", null, invokeRetrieveAccountFlow(checkAccountflow, createdAccounts.get(1)));

		Map<String, Object> payload = invokeRetrieveAccountFlow(checkAccountflow, createdAccounts.get(2));
		assertEquals("The account should have been sync", createdAccounts.get(2)
																			.get("Name"), payload.get("Name"));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> invokeRetrieveAccountFlow(SubflowInterceptingChainLifecycleWrapper flow, Map<String, Object> account) throws Exception {
		Map<String, Object> accountMap = new HashMap<String, Object>();

		accountMap.put("Name", account.get("Name"));

		MuleEvent event = flow.process(getTestEvent(accountMap, MessageExchangePattern.REQUEST_RESPONSE));
		Object payload = event.getMessage()
								.getPayload();
		if (payload instanceof NullPayload) {
			return null;
		} else {
			return (Map<String, Object>) payload;
		}
	}

	@SuppressWarnings("unchecked")
	private void createTestDataInSandBox() throws MuleException, Exception {
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("createAccountFlow");
		flow.initialise();

		// This account should not be sync as the industry is not Government nor Education
		createdAccounts.add(anAccount().with("Name", buildUniqueName(KICK_NAME, "NotSyncOne"))
										.with("BillingCity", "San Francisco")
										.with("BillingCountry", "USA")
										.with("Phone", "123456789")
										.with("Industry", "Insurance")
										.with("NumberOfEmployees", 8000)
										.build());

		// This account should not be sync as the number of employees is smaller than 7000
		createdAccounts.add(anAccount().with("Name", buildUniqueName(KICK_NAME, "NotSyncTwo"))
										.with("BillingCity", "San Francisco")
										.with("BillingCountry", "USA")
										.with("Phone", "123456789")
										.with("Industry", "Education")
										.with("NumberOfEmployees", 5000)
										.build());

		// This account should BE sync
		createdAccounts.add(anAccount().with("Name", buildUniqueName(KICK_NAME, "YesSync"))
										.with("BillingCity", "San Francisco")
										.with("BillingCountry", "USA")
										.with("Phone", "123456789")
										.with("Industry", "Education")
										.with("NumberOfEmployees", 9000)
										.build());

		MuleEvent event = flow.process(getTestEvent(createdAccounts, MessageExchangePattern.REQUEST_RESPONSE));
		List<SaveResult> results = (List<SaveResult>) event.getMessage()
															.getPayload();
		for (int i = 0; i < results.size(); i++) {
			createdAccounts.get(i)
							.put("Id", results.get(i)
												.getId());
		}

		System.out.println("Results of data creation in sandbox" + createdAccounts.toString());
	}

	private void deleteTestDataFromSandBox() throws MuleException, Exception {
		// Delete the created accounts in A
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("deleteAccountFromAFlow");
		flow.initialise();

		List<Object> idList = new ArrayList<Object>();
		for (Map<String, Object> c : createdAccounts) {
			idList.add(c.get("Id"));
		}
		flow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));

		// Delete the created accounts in B
		flow = getSubFlow("deleteAccountFromBFlow");
		flow.initialise();
		idList.clear();
		for (Map<String, Object> c : createdAccounts) {
			Map<String, Object> account = invokeRetrieveAccountFlow(checkAccountflow, c);
			if (account != null) {
				idList.add(account.get("Id"));
			}
		}
		flow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}
}
