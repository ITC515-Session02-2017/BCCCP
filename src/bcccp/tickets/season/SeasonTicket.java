package bcccp.tickets.season;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

public class SeasonTicket implements ISeasonTicket {
	
	private List<IUsageRecord> usages;
	private IUsageRecord currentUsage = null;
	
	private String ticketId;
	private String carparkId;
	private long startValidPeriod;
	private long endValidPeriod;
	
	public SeasonTicket (String ticketId, 
			             String carparkId, 
			             long startValidPeriod,
			             long endValidPeriod) {

		this.ticketId = ticketId;
		this.carparkId = carparkId;
		this.startValidPeriod = startValidPeriod;
		this.endValidPeriod = endValidPeriod;

		usages = new ArrayList<>();
	}

	@Override
	public String getId() {
		return ticketId;
	}

	@Override
	public String getCarparkId() {
		return carparkId;
	}

	@Override
	public long getStartValidPeriod() {
		return startValidPeriod;
	}

	@Override
	public long getEndValidPeriod() {
		return endValidPeriod;
	}

	@Override
	public boolean inUse() {

	    // Check if the season ticket is already in use (check against the usage list)

		Iterator<IUsageRecord> usageRec = usages.iterator();

		boolean foundUsageRecord = false;

		while (usageRec.hasNext()) {

		    if (usageRec.next().getSeasonTicketId().equals(getId())) {

                foundUsageRecord = true;
            }

            else foundUsageRecord = false;
        }

        return foundUsageRecord;
	}

	@Override
	public void recordUsage(IUsageRecord record) {

	    // Add a usage record of this season ticket to the usage List (usages)
		// This method accepts an existing record as argument.

        usages.add(record);
	}

	@Override
	public IUsageRecord getCurrentUsageRecord() {

	    // Find current usage record in the usages List and return the usage record

        Iterator<IUsageRecord> usageRecs = usages.iterator();

        while (usageRecs.hasNext()) {

            if (usageRecs.next().getSeasonTicketId().equals(getId())) {

                currentUsage = usageRecs.next();
            }
        }

        return currentUsage;
	}

	@Override
	public void endUsage(long dateTime) {

	    // Records the date and time of the end of this usage of the Season ticket.
		// Will need to get the current usage record and write the dateTime to the usage record
		// (found in the 'usages' list above)
		// use the getSeasonTicketID() in the usageRecord

		Iterator<IUsageRecord> usageRecs = usages.iterator();

		while (usageRecs.hasNext()) {

			if (usageRecs.next().getSeasonTicketId().equals(getId())) {

				usageRecs.next().finalise(dateTime);
			}
		}
	}

	@Override
	public List<IUsageRecord> getUsageRecords() {

	    // Get the entire list of usage records and return as List

        return usages;
	}


}
