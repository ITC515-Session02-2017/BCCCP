package bcccp.tickets.adhoc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegAdhocTicketFactoryTest {
  static AdhocTicketFactory adhocTicketFactory;
  static IAdhocTicket iadhocTicket;
  static AdhocTicketDAO adhocTicketDAO;

  Logger logger = Logger.getLogger("Integration testing for AdhocTicketFactory class");

  @BeforeAll
  static void before() {

    adhocTicketFactory = new AdhocTicketFactory();

  }

  @Test
  void integTestBarcodeGeneration() {

    logger.log(Level.INFO, "Integration Test barcode generation");

    AdhocTicketFactory ad = new AdhocTicketFactory();

    IAdhocTicket ticket = ad.make("123", 123);

    assertEquals(ad.generateBarCode(123, ad.entryDate()), ticket.getBarcode());

  }

  @Test
  void integTestMakeTicketNum() {

    logger.log(Level.INFO, "Integration Test make method - Ticket No.");

    iadhocTicket = adhocTicketFactory.make("Integral Carpark #1", 47);

    assertEquals(iadhocTicket.getTicketNo(), 47);

  }

  @Test
  void integTestMakeCarparkId() {

    logger.log(Level.INFO, "Integration Test make method - Carpark Id");

    iadhocTicket = adhocTicketFactory.make("Integral Carpark #1", 47);

    assertEquals(iadhocTicket.getCarparkId(), "Integral Carpark #1");

  }

  @Test
  void integTestMakeEntryDate() {

    logger.log(Level.INFO, "Integration Test make method - Entry Date");

    iadhocTicket = adhocTicketFactory.make("Integral Carpark #1", 47);

    assertEquals(adhocTicketFactory.entryDate().substring(4, 8), "2017");

  }
}