/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.util;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * The function of this class is to establish a relation happens before between two maps representing SFDC opportunities.
 * 
 * It's assumed that these maps are well formed maps from SFDC thus they both contain an entry with the expected key. Never the less validations are being done.
 * 
 */
public class OpportunityDateComparator {
	private static final String LAST_MODIFIED_DATE = "LastModifiedDate";

	/**
	 * Validate which opportunity has the latest last referenced date.
	 * 
	 * @param opportunityA
	 *            SFDC opportunity map
	 * @param opportunityB
	 *            SFDC opportunity map
	 * @return true if the last activity date from opportunityA is after the one from opportunityB
	 */
	public static boolean isAfter(Map<String, String> opportunityA, Map<String, String> opportunityB) {
		Validate.notNull(opportunityA, "The opportunity A should not be null");
		Validate.notNull(opportunityB, "The opportunity B should not be null");

		Validate.isTrue(opportunityA.containsKey(LAST_MODIFIED_DATE), "The opportunity A map should containt the key " + LAST_MODIFIED_DATE);

		if (opportunityB.get(LAST_MODIFIED_DATE) == null) {

			return true;

		} else {

			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			DateTime LastModifiedDateOfA = formatter.parseDateTime(opportunityA.get(LAST_MODIFIED_DATE));
			DateTime LastModifiedDateOfB = formatter.parseDateTime(opportunityB.get(LAST_MODIFIED_DATE));

			return LastModifiedDateOfA.isAfter(LastModifiedDateOfB);

		}

	}
}
