package bcccp.tickets.adhoc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Data Access Object providing an interface to a database of tickets
 */
public class AdhocTicketDAO implements IAdhocTicketDAO {

  private IAdhocTicketFactory factory;

    List<IAdhocTicket> list;

  private int currentTicketNo;

  public AdhocTicketDAO(IAdhocTicketFactory factory) {

    this.factory = factory;

      list = new ArrayList<>();
  }

  @Override
  public IAdhocTicket createTicket(String carparkId) {

      IAdhocTicket ticket = factory.make(carparkId, ++currentTicketNo);

      list.add(ticket);

    return ticket;
  }

  @Override
  public IAdhocTicket findTicketByBarcode(String barcode) {

      IAdhocTicket ticket = null;

      Iterator<IAdhocTicket> itr = list.iterator();

      while (itr.hasNext()) {

          IAdhocTicket tkt = itr.next();

          if (tkt.getBarcode().equals(barcode)) {

              ticket = tkt;

              break;
          }
      }

      return ticket;
  }

  @Override
  public List<IAdhocTicket> getCurrentTickets() {
      return list.stream().filter(c -> c.isCurrent()).collect(Collectors.toList());
  }

}
