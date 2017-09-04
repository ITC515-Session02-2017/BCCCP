package bcccp.tickets.adhoc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A ticket dispensation utility class.
 */
public class AdhocTicketFactory implements IAdhocTicketFactory {

  @Override
  public IAdhocTicket make(String carparkId, int ticketNo) {

    return new AdhocTicket(carparkId, ticketNo, generateBarCode(ticketNo, entryDate()));
  }

  /**
   * Description <br>
   * -the ticket issued to each ordinary customer has a bar code on it. The barcode <br>
   * has a number on it and the date (ddmmyyyy) and time (hhmmss) of entry to the car park. <br>
   *
   * @return String
   */
  private String entryDate() {

    // Display a date in day, month, year format
    DateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");

    return formatter.format(new Date().getTime()); // the string that is encoded (to a bar code)
  }

  private String generateBarCode(int ticketNum, String entryDate) {

    String prefix = "0041"; // hex representation of "A". Unicode: U+0041

    String hexNum = Integer.toHexString(ticketNum);

    String hexDate = Integer.toHexString(Integer.parseInt(entryDate));
    // insert delimiter ":" between hex values
    return new StringBuilder(prefix + ":").append(hexNum + ":").append(hexDate).toString();
  }

}