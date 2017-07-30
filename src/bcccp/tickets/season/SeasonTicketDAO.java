package bcccp.tickets.season;

import bcccp.tickets.season.ISeasonTicket;
import bcccp.tickets.season.IUsageRecordFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

public class SeasonTicketDAO implements ISeasonTicketDAO {

	private List<ISeasonTicket> seasonTickets;
	private IUsageRecordFactory factory;

	
	
	public SeasonTicketDAO(IUsageRecordFactory factory) {

		this.factory = factory;

		seasonTickets = new ArrayList<>();

	}



	@Override
	public void registerTicket(ISeasonTicket ticket) {

		seasonTickets.add(ticket);
		
	}


	@Override
	public void deregisterTicket(ISeasonTicket ticket) {

		// 'deregister' means: remove ticket from the seasonTickets ArrayList

		Iterator<ISeasonTicket> sTicketRecs = seasonTickets.iterator();

		while (sTicketRecs.hasNext()) {

			if (sTicketRecs.next().getId().equals(ticket.getId())) {

				sTicketRecs.remove();
			}
		}
		
	}


	@Override
	public int getNumberOfTickets() {

		return seasonTickets.size();
	}


	@Override
	public ISeasonTicket findTicketById(String ticketId) {

		Iterator<ISeasonTicket> sTicketRecs = seasonTickets.iterator();

		ISeasonTicket sTicket = null;

		while (sTicketRecs.hasNext()) {

			if (sTicketRecs.next().getId().equals(ticketId)) {

				sTicket = sTicketRecs.next();
			}
			else {
				sTicket = null;
			}
		}
		return sTicket;
	}


	@Override
	public void recordTicketEntry(String ticketId) {

		// This method creates a new usage record with current day and time as the startTime
		// and uses recordUsage method from SeasonTicket class to record it to the ArrayList

		Date dateTime = new Date();

		IUsageRecord usageRecord = factory.make(ticketId, dateTime.getTime());

		findTicketById(ticketId).recordUsage(usageRecord);

	}


	@Override
	public void recordTicketExit(String ticketId) {

		// Finds an existing usage record and records the current day and time (on exiting of vehicle)
		// on the record

		Date dateTime = new Date();

		findTicketById(ticketId).endUsage(dateTime.getTime());
		
	}
	
	
}
