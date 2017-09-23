package bcccp.tickets.adhoc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;


class IntegAdhocTicketDAOTest {

  static AdhocTicketDAO adhocTicketDAO;
  static IAdhocTicket adhocTicket;
  static AdhocTicketFactory adhocTicketFactory;

  Logger logger = Logger.getLogger("Integration Test Unit for AdhocTicketDAO class");


  @BeforeAll
  static void setupAllTests() {

  }

  @BeforeEach
  void setupEachTest() {

    adhocTicketFactory = new AdhocTicketFactory();

    adhocTicket = adhocTicketFactory.make("Integral Carpark #2", 51);

  }

  @AfterEach
  void cleanupEachTest() {

    adhocTicketFactory = null;

    adhocTicket = null;

  }

  @Test
  void integTestClassImplementationWithNull() {

    logger.log(Level.INFO, "Integration Testing the class constructor with null");

    adhocTicketDAO = new AdhocTicketDAO(null);

    Throwable exception = assertThrows(RuntimeException.class, () -> {

      throw new RuntimeException("Error: null in class constructor");
    });
    assertEquals("Error: null in class constructor", exception.getMessage());
  }

  @Test
  void integTestCreateTicket() {

    logger.log(Level.INFO, "Integration test createTicket method");

    adhocTicketDAO = new AdhocTicketDAO(adhocTicketFactory);

    adhocTicket = adhocTicketDAO.createTicket("123");

    assertEquals(1, adhocTicket.getTicketNo());
  }

  @Test
  void integTestFindTicketwithTicketdoesntExist() {

    logger.log(Level.INFO, "Integration Test findTicket method with non existing ticket. " +
            "Should return null");

    AdhocTicketDAO dao = new AdhocTicketDAO(adhocTicketFactory);

    IAdhocTicket t = dao.findTicketByBarcode("123");

    assertEquals(null, t);
  }

  @Test
  void integTestGetCurrentTickets() {

    logger.log(Level.INFO, "Integration Test getCurrentTickets method with list of " +
            "current tickets");

    AdhocTicketDAO dao = new AdhocTicketDAO(adhocTicketFactory);

    IAdhocTicket t1 = dao.createTicket("120");

    IAdhocTicket t2 = dao.createTicket("121");

    IAdhocTicket t3 = dao.createTicket("122");

    IAdhocTicket t4 = dao.createTicket("123");

    t1.enter(1L);

    t2.enter(1L);

    t3.enter(1L);

    t4.enter(1L);

    List<IAdhocTicket> list = dao.getCurrentTickets();

    assertEquals(4, list.size());

    assertTrue(t1.isCurrent());

    assertTrue(t2.isCurrent());

    assertTrue(t3.isCurrent());

    assertTrue(t4.isCurrent());

  }

  @Test
  void integTestGetCurrentTicketsEmptyList() {

    logger.log(Level.INFO, "Integration Test getCurrentTickets method with " +
            "no adhoc tickets currently in use");

    AdhocTicketDAO dao = new AdhocTicketDAO(adhocTicketFactory);

    List<IAdhocTicket> list = dao.getCurrentTickets();

    assertEquals(0, list.size());

  }

}