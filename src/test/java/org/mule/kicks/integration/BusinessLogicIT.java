package org.mule.kicks.integration;

import static org.junit.Assert.assertEquals;
import static org.mule.kicks.builders.SfdcObjectBuilder.anOpportunity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
 * The test will invoke the batch process and afterwards check that the opportunities had been correctly created and that the ones that should be filtered are
 * not in the destination sand box.
 * 
 */
public class BusinessLogicIT extends AbstractKickTestCase {

	protected static final int TIMEOUT_SECONDS = 60;

	private static final String KICK_NAME = "sfdc2sfdc-opportunity-migration";

	private BatchTestHelper helper;

	private static SubflowInterceptingChainLifecycleWrapper checkOpportunityflow;
	private static List<Map<String, Object>> createdOpportunities = new ArrayList<Map<String, Object>>();

	@Rule
	public DynamicPort port = new DynamicPort("http.port");

	@Before
	public void setUp() throws Exception {
		helper = new BatchTestHelper(muleContext);

		checkOpportunityflow = getSubFlow("retrieveOpportunityFlow");
		checkOpportunityflow.initialise();

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

		assertEquals("The opportunity should not have been sync", null, invokeRetrieveOpportunityFlow(checkOpportunityflow, createdOpportunities.get(0)));

		assertEquals("The opportunity should not have been sync", null, invokeRetrieveOpportunityFlow(checkOpportunityflow, createdOpportunities.get(1)));

		Map<String, Object> payload = invokeRetrieveOpportunityFlow(checkOpportunityflow, createdOpportunities.get(2));
		assertEquals("The opportunity should have been sync", createdOpportunities.get(2)
																					.get("Name"), payload.get("Name"));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> invokeRetrieveOpportunityFlow(SubflowInterceptingChainLifecycleWrapper flow, Map<String, Object> opportunity) throws Exception {
		Map<String, Object> opportunityMap = new HashMap<String, Object>();

		opportunityMap.put("Name", opportunity.get("Name"));

		MuleEvent event = flow.process(getTestEvent(opportunityMap, MessageExchangePattern.REQUEST_RESPONSE));
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
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("createOpportunityFlow");
		flow.initialise();

		// This opportunity should not be synced as amount is below 5000
		createdOpportunities.add(anOpportunity().with("Name", buildUniqueName(KICK_NAME, "NotSyncOne"))
												.with("CloseDate", date("2032-06-12"))
												.with("Amount", 12)
												.with("StageName", "MyStage1")
												.build());

		// This opportunity should not be synced as amount is below 5000
		createdOpportunities.add(anOpportunity().with("Name", buildUniqueName(KICK_NAME, "NotSyncTwo"))
												.with("CloseDate", date("2040-02-04"))
												.with("Amount", 1200.0)
												.with("StageName", "MyStage2")
												.build());

		// This opportunity should BE synced
		createdOpportunities.add(anOpportunity().with("Name", buildUniqueName(KICK_NAME, "YesSync"))
												.with("CloseDate", date("2070-03-13"))
												.with("Amount", 5001)
												.with("StageName", "MyStage3")
												.build());

		MuleEvent event = flow.process(getTestEvent(createdOpportunities, MessageExchangePattern.REQUEST_RESPONSE));
		List<SaveResult> results = (List<SaveResult>) event.getMessage()
															.getPayload();
		for (int i = 0; i < results.size(); i++) {
			createdOpportunities.get(i)
								.put("Id", results.get(i)
													.getId());
		}

		System.out.println("Results of data creation in sandbox" + createdOpportunities.toString());
	}

	private Date date(String dateString) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
	}

	private void deleteTestDataFromSandBox() throws MuleException, Exception {
		// Delete the created opportunities in A
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("deleteOpportunityFromAFlow");
		flow.initialise();

		List<Object> idList = new ArrayList<Object>();
		for (Map<String, Object> c : createdOpportunities) {
			idList.add(c.get("Id"));
		}
		flow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));

		// Delete the created opportunities in B
		flow = getSubFlow("deleteOpportunityFromBFlow");
		flow.initialise();
		idList.clear();
		for (Map<String, Object> c : createdOpportunities) {
			Map<String, Object> opportunity = invokeRetrieveOpportunityFlow(checkOpportunityflow, c);
			if (opportunity != null) {
				idList.add(opportunity.get("Id"));
			}
		}
		flow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}
}
