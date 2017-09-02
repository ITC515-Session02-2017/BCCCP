package bcccp.tickets.adhoc;

import java.util.*;


/**
 * A Data Access Object providing an interface to a database of tickets
 */
public final class AdhocTicketDAO implements IAdhocTicketDAO {

  private IAdhocTicketFactory factory;

  private Map<String, IAdhocTicket> currentTickets;

  private int currentTicketNo;

  public AdhocTicketDAO(IAdhocTicketFactory factory) {

    this.factory = factory;

    currentTickets = new HashMap<>();
  }

  @Override
  public IAdhocTicket createTicket(String carparkId) {

    IAdhocTicket ticket = factory.make(carparkId, currentTicketNo++);

    currentTickets.put(ticket.getBarcode(), ticket);

    return ticket;
  }

  @Override
  public IAdhocTicket findTicketByBarcode(String barcode) {

    return currentTickets.get(barcode);
  }

  @Override
  public List<IAdhocTicket> getCurrentTickets() {
    return Collections.unmodifiableList(new ArrayList<IAdhocTicket>(currentTickets.values()));
  }

}
