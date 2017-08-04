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

        usages.add(record);
	}

	@Override
	public IUsageRecord getCurrentUsageRecord() {

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

		Iterator<IUsageRecord> usageRecs = usages.iterator();

		while (usageRecs.hasNext()) {

			if (usageRecs.next().getSeasonTicketId().equals(getId())) {

				usageRecs.next().finalise(dateTime);
			}
		}
	}

	@Override
	public List<IUsageRecord> getUsageRecords() {

        return usages;
	}


}
