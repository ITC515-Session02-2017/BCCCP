package bcccp.tickets.adhoc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdhocTicketFactory implements IAdhocTicketFactory {

  @Override
  public IAdhocTicket make(String carparkId, int ticketNo) {

    return new AdhocTicket(carparkId, ticketNo, generateBarCode());
  }

  /**
   * Description <br> -the ticket issued to each ordinary customer has a bar code on it. The barcode
   * <br> has a number on it and the date (ddmmyyyy) and time (hhmmss) of entry to the car park.
   * <br>
   *
   * @return String
   */
  private String generateBarCode() {

    // Display a date in day, month, year format
    DateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");

    return formatter.format(new Date().getTime()); // the string that is encoded (to a bar code)
  }
}
