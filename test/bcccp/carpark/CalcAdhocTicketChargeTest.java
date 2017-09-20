package bcccp.carpark;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import bcccp.tickets.adhoc.AdhocTicket;
import bcccp.tickets.adhoc.AdhocTicketDAO;
import bcccp.tickets.adhoc.AdhocTicketFactory;
import bcccp.tickets.adhoc.IAdhocTicket;
import bcccp.tickets.adhoc.IAdhocTicketDAO;
import bcccp.tickets.season.ISeasonTicketDAO;
import bcccp.tickets.season.SeasonTicketDAO;
import bcccp.tickets.season.UsageRecordFactory;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;

class CalcAdhocTicketChargeTest {

  Date entryDate, exitDate;

  String entryStrDate, exitStrDate;

  BigDecimal result;

  static IAdhocTicketDAO adhocTicketDAO;

  static ISeasonTicketDAO seasonTicketDAO;

  static Carpark cp;

  static Carpark testItem;

  static String DEFAULT_CARPARK = "Alphabet Street";

  static int DEFAULT_CAPACITY = 3;

  private Logger logger = Logger
      .getLogger("Unit testing for Carpark's charge calculation methodology.");


  @BeforeAll
  static void before() {

    adhocTicketDAO = spy(new AdhocTicketDAO(new AdhocTicketFactory()));

    seasonTicketDAO = spy(new SeasonTicketDAO(new UsageRecordFactory()));

    testItem = new Carpark(DEFAULT_CARPARK, DEFAULT_CAPACITY, adhocTicketDAO, seasonTicketDAO);


  }

  @Test
  void workingHoursAddHocTicketCharge() {

    BigDecimal val = new BigDecimal(5);

    // Display a date in day, month, year format
    DateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");

    entryStrDate = "02042013103542"; // "02-04-2013 10:35:42"

    try {

      entryDate = formatter.parse(entryStrDate);

    } catch (ParseException e) {

      e.printStackTrace();
    }

    exitStrDate = "02042013113542"; // entrydate + 1 hour: "02-04-2013 11:35:42"

    try {

      exitDate = formatter.parse(exitStrDate);

    } catch (ParseException e) {

      e.printStackTrace();
    }

    IAdhocTicket ticket = new AdhocTicket(testItem.getName(), 1, generateBarCode(1, entryStrDate));

    SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");

    logger.log(Level.INFO, "Equivalence partition for: WORKING HOURS ");

    logger.log(Level.INFO, "Entry Date: " + ft.format(ticket.getEntryDateTime()));

    logger.log(
        Level.INFO,
        "Checkout Date: " + ft.format(ticket.getEntryDateTime() + TimeUnit.HOURS.toMillis(1)));

    result = CalcAdhocTicketCharge.calcCharge(entryDate.getTime(), exitDate.getTime());

    assertEquals(val, result);

  }

  @Test
  void outOfHoursAhocTicketcCharge() {

    BigDecimal val = new BigDecimal(4);

    // Display a date in day, month, year format
    DateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");

    entryStrDate = "02042013203542"; // "02-04-2013 20:35:42"

    try {

      entryDate = formatter.parse(entryStrDate);

    } catch (ParseException e) {

      e.printStackTrace();
    }

    exitStrDate = "02042013223542"; // entrydate + 2 hour: "02-04-2013 22:35:42"

    try {

      exitDate = formatter.parse(exitStrDate);

    } catch (ParseException e) {

      e.printStackTrace();
    }

    IAdhocTicket ticket = new AdhocTicket(testItem.getName(), 2, generateBarCode(2, entryStrDate));

    SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");

    logger.log(Level.INFO, "Equivalence partition for: OUT OF HOURS ");

    logger.log(Level.INFO, "Entry Date: " + ft.format(ticket.getEntryDateTime()));

    logger.log(
        Level.INFO,
        "Checkout Date: " + ft.format(ticket.getEntryDateTime() + TimeUnit.HOURS.toMillis(2)));

    result = CalcAdhocTicketCharge.calcCharge(entryDate.getTime(), exitDate.getTime());

    // two dollars an hour for two hour = 4 dollars (at "out of hours" rate)

    assertEquals(val, result);

  }


  @Test
  void mixedHoursAdhocTicketCharge() {

    BigDecimal val = new BigDecimal(4);

    // Display a date in day, month, year format
    DateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");

    entryStrDate = "02042013053542"; // "02-04-2013 05:35:42"

    try {

      entryDate = formatter.parse(entryStrDate);

    } catch (ParseException e) {

      e.printStackTrace();
    }

    exitStrDate = "02042013093542"; // entrydate + 2 hour: "02-04-2013 09:35:42"

    try {

      exitDate = formatter.parse(exitStrDate);

    } catch (ParseException e) {

      e.printStackTrace();
    }

    IAdhocTicket ticket = new AdhocTicket(testItem.getName(), 3, generateBarCode(3, entryStrDate));

    SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");

    logger.log(Level.INFO, "Boundary test for: OUT OF HOURS and WORKING HOURS");

    logger.log(Level.INFO, "Entry Date: " + ft.format(ticket.getEntryDateTime()));

    logger.log(
        Level.INFO,
        "Checkout Date: " + ft.format(ticket.getEntryDateTime() + TimeUnit.HOURS.toMillis(2)));

    result = CalcAdhocTicketCharge.calcCharge(entryDate.getTime(), exitDate.getTime());

    // two dollars an hour for two hour = 4 dollars (at "out of hours" rate)
    // plus five dollars an hour for two hours = $14

    assertEquals(val, result);

  }


  /**
   * utility for generating the barcode (from adhocTicket class)
   */
  private String generateBarCode(int ticketNum, String entryDate) {

    String prefix = "0041"; // hex representation of "A". Unicode: U+0041

    String hexNum = Integer.toHexString(ticketNum);

    String hexDate = null;
    try {
      hexDate = toHexadecimal(entryDate);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    return prefix + "\u002D" + hexNum + "\u002D" + hexDate;
  }


  private static String toHexadecimal(String text) throws UnsupportedEncodingException {
    byte[] myBytes = text.getBytes("UTF-16");

    return DatatypeConverter.printHexBinary(myBytes);
  }

}