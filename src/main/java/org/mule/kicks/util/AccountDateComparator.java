package org.mule.kicks.util;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * The function of this class is to establish a relation happens before between
 * two maps representing SFDC accounts.
 * 
 * It's assumed that these maps are well formed maps from SFDC thus they both
 * contain an entry with the expected key. Never the less validations are being
 * done.
 * 
 * @author cesargarcia
 */
public class AccountDateComparator {
	private static final String LAST_MODIFIED_DATE = "LastModifiedDate";

	/**
	 * Validate which account has the latest last referenced date.
	 * 
	 * @param accountA
	 *            SFDC account map
	 * @param accountB
	 *            SFDC account map
	 * @return true if the last activity date from accountA is after the one
	 *         from accountB
	 */
	public static boolean isAfter(Map<String, String> accountA, Map<String, String> accountB) {
		Validate.notNull(accountA, "The account A should not be null");
		Validate.notNull(accountB, "The account B should not be null");

		Validate.isTrue(accountA.containsKey(LAST_MODIFIED_DATE), "The account A map should containt the key " + LAST_MODIFIED_DATE);

		if (accountB.get(LAST_MODIFIED_DATE) == null ) {
			
			return true;
			
		} else { 
			
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			DateTime LastModifiedDateOfA = formatter.parseDateTime(accountA.get(LAST_MODIFIED_DATE));
			DateTime LastModifiedDateOfB = formatter.parseDateTime(accountB.get(LAST_MODIFIED_DATE));
			
			return LastModifiedDateOfA.isAfter(LastModifiedDateOfB);
			
		}
		
	}
}
