package bcccp.carpark.paystation;

import bcccp.carpark.Carpark;
import bcccp.carpark.entry.EntryController;
import bcccp.tickets.adhoc.AdhocTicketDAO;
import bcccp.tickets.adhoc.AdhocTicketFactory;
import bcccp.tickets.adhoc.IAdhocTicket;
import bcccp.tickets.adhoc.IAdhocTicketDAO;
import bcccp.tickets.season.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;


public class IntegCarparkTest {

  static IAdhocTicketDAO adhocTicketDAO;

  static ISeasonTicketDAO seasonTicketDAO;

  static Carpark cp;

  static Carpark testItem;

  static EntryController entryController;

  static String DEFAULT_CARPARK = "Alphabet Street";

  static int DEFAULT_CAPACITY = 3;

  private Logger logger = Logger.getLogger("Unit testing for Carpark class.");


  @BeforeAll
  static void before() {

    adhocTicketDAO = spy(new AdhocTicketDAO(new AdhocTicketFactory()));

    seasonTicketDAO = spy(new SeasonTicketDAO(new UsageRecordFactory()));

    testItem = new Carpark(DEFAULT_CARPARK, DEFAULT_CAPACITY, adhocTicketDAO, seasonTicketDAO);

    entryController = mock(EntryController.class);

  }


  @AfterEach
  void after() {


    adhocTicketDAO = spy(new AdhocTicketDAO(new AdhocTicketFactory()));

    seasonTicketDAO = spy(new SeasonTicketDAO(new UsageRecordFactory()));

    testItem = new Carpark(DEFAULT_CARPARK, DEFAULT_CAPACITY, adhocTicketDAO, seasonTicketDAO);


  }


  /* NOTE: Unchecked exceptions do not need to be declared in a method or constructor's throws clause if they can be thrown
  by the execution of the method. See: https://docs.oracle.com/javase/7/docs/api/java/lang/RuntimeException.html
  What is actually required by the following test is for the constructor to throw an illegalArgumentException under specified conditions
  (which extends RuntimeException). */
  @Test
  void isValidConstruct() {

    logger.log(Level.INFO, "Testing exception handling for carpark constructor...");


    // invalid 'name' argument: null
    try {

      Carpark testItem = new Carpark(null, 3, adhocTicketDAO, seasonTicketDAO);

      fail("Expected a RuntimeException to be thrown");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor: carparkId is null",
          e.getMessage());
    }

    // invalid 'name' argument: empty
    try {

      Carpark testitem = new Carpark("", 3, adhocTicketDAO, seasonTicketDAO);

      fail("Expected a RuntimeException to be thrown");

    } catch (Exception e) {

      assertEquals("Invalid argument passed to Carpark constructor: carparkId is empty",
          e.getMessage());
    }

    // invalid 'capacity' argument: empty
    try {

      Carpark testItem = new Carpark("Bathurst Chase", 0, adhocTicketDAO, seasonTicketDAO);

      fail("Expected a RuntimeException to be thrown");

    } catch (Exception e) {

      assertEquals("Invalid argument passed to Carpark constructor: capacity is zero or negative",
          e.getMessage());
    }

    // invalid 'capacity' argument: negative
    try {

      Carpark testItem = new Carpark("Bathurst Chase", -1, adhocTicketDAO, seasonTicketDAO);

      fail("Expected a RuntimeException to be thrown");

    } catch (Exception e) {

      assertEquals("Invalid argument passed to Carpark constructor: capacity is zero or negative",
          e.getMessage());
    }
  }

  @Test
  /** returns the carpark name */
  void getName() {

    logger.log(Level.INFO, "Testing getName...");

    assertEquals("Alphabet Street", testItem.getName());
  }

  @Test
  /** returns a boolean indicating whether the carpark is full (ie no adhoc spaces available) */
  void isFull() {

    logger.log(Level.INFO, "Testing isFull...");
    //cars + 1
    testItem.recordAdhocTicketEntry();
    //cars + 1
    testItem.recordAdhocTicketEntry();
    //cars + 1
    testItem.recordAdhocTicketEntry();

    assertEquals(true, testItem.isFull());

  }

  @Test
  /**
   * if spaces for adhoc parking are available returns a valid new AdhocTicket throws a
   * RuntimeException if called when carpark is full (ie no adhoc spaces available)
   */
  void issueAdhocTicket() {

    logger.log(Level.INFO, "Testing issueAdhocTicket...");

    testItem.issueAdhocTicket();

    verify(adhocTicketDAO).createTicket("Alphabet Street");

  }

  @Test
  /**
   * registers observer as an entity to be notified through the notifyCarparkEvent method when the
   * carpark is full and spaces become available
   */
  void register() {


    logger.log(Level.INFO, "EntryController added....");

    testItem.register(entryController);


  }

  @Test
  /** remove observer as an entity to be notified */
  void deregister() {


    logger.log(Level.INFO, "EntryController removed....");

    testItem.deregister(entryController);


  }

  @Test
  void recordAdhocTicketExit() {

    logger.log(Level.INFO, "Testing recordAdhocTicketExit...");
    //cars + 1
    testItem.recordAdhocTicketEntry();
    //cars + 1
    testItem.recordAdhocTicketEntry();
    //cars + 1 = capacity = 3 = full
    testItem.recordAdhocTicketEntry();

    if (testItem.isFull())
      logger.log(Level.INFO, "Carpark has reached capacity...");
    //3 - 1 = 2  (capacity - 1)
    testItem.recordAdhocTicketExit();
    logger.log(Level.INFO, "Carpark has a space available...");

    assertEquals(false, testItem.isFull());


  }

  @Test
  /**
   * increments the number of adhoc carpark spaces in use. May cause the carpark to become full (ie
   * all adhoc spaces filled)
   */
  void recordAdhocTicketEntry() {

    logger.log(Level.INFO, "Testing recordAdhocTicketEntry...");
    //cars + 1
    testItem.recordAdhocTicketEntry();
    //cars + 1
    testItem.recordAdhocTicketEntry();
    //car + 1
    testItem.recordAdhocTicketEntry();

    assertEquals(true, testItem.isFull());

  }

  @Test
  /**
   * returns the adhoc ticket identified by the barcode, returns null if the ticket does not exist,
   * or is not current (ie not in use).
   */
  void getAdhocTicket() {

    logger.log(Level.INFO, "Testing getAdhocTicket...");

    IAdhocTicket expected = testItem.issueAdhocTicket();

    IAdhocTicket ticket = testItem.getAdhocTicket(expected.getBarcode());

    // This test is failing when only one ticket has been issued.
    assertEquals(expected.getEntryDateTime(), ticket.getEntryDateTime());


  }

  @Test
  /**
   * registers a season ticket with the carpark so that the season ticket may be used to access the
   * carpark.
   *
   * Note this test will fail because there is no usage record - requires ExitController.ticketInserted()
   * to call SeasonTicketDAO.recordTicketEntry().
   */
  void registerSeasonTicket() {

    logger.log(Level.INFO, "Testing registerSeasonTicket...");

    ISeasonTicket tktA = mock(SeasonTicket.class);

    doReturn("S2222").when(tktA).getId();

    doReturn("Alphabet Street").when(tktA).getCarparkId();

    testItem.registerSeasonTicket(tktA);

    verify(seasonTicketDAO).registerTicket(tktA);

  }

  /**
   * Throws a RuntimeException if the carpark the season ticket is associated with is not the
   * same as the carpark name.
   */
  @Test
  void registerSeasonTicketRuntimeExceptionTest() {

    try {

      testItem.registerSeasonTicket(new SeasonTicket("S9999", "Wrong Name", 0L, 0L));

      fail("Expected a RuntimeException to be thrown");

    } catch (Exception e) {

      assertEquals(
          "SeasonTicket in registerSeasonTicket has invalid CarparkId: Wrong Name, should be CarparkId: Alphabet Street",
          e.getMessage());
    }

  }


  @Test
  /**
   * deregisters the season ticket so that the season ticket may no longer be used to access the
   * carpark
   */
  void deregisterSeasonTicket() {


    logger.log(Level.INFO, "Testing deregisterSeasonTicket...");

    ISeasonTicket tkt = mock(SeasonTicket.class);

    when(tkt.getId()).thenReturn("S2222");

    when(tkt.getCarparkId()).thenReturn("Alphabet Street");

    testItem.registerSeasonTicket(tkt);

    testItem.deregisterSeasonTicket(tkt);

    verify(seasonTicketDAO).deregisterTicket(tkt);

  }

  @Test
  void isSeasonTicketValid() {

    //not tested (valid if other methods are tested and passing)
  }



  /**
   * causes a new usage record to be created and associated with a season ticket.
   */
  @Test
  void recordSeasonTicketEntry() {

    logger.log(Level.INFO, "Testing recordSeasonTicketEntry...");

    ISeasonTicket tkt = mock(SeasonTicket.class);

    when(tkt.getId()).thenReturn("S2222");

    when(tkt.getCarparkId()).thenReturn("Alphabet Street");

    testItem.registerSeasonTicket(tkt);

    testItem.recordSeasonTicketEntry(tkt.getId());

    verify(seasonTicketDAO).recordTicketEntry("S2222");

  }


  /**
   * Throws a
   * RuntimeException if the season ticket associated with ticketId does not exist, or is currently
   * in use
   */
  @Test
  void recordSeasonTicketEntryRuntimeExceptionTest() {

    try {
      //no ticket has the following id:
      testItem.recordSeasonTicketEntry("badId");

      fail("Expected a RuntimeException to be thrown");

    } catch (Exception e) {

      assertEquals("Runtime Exception: No corresponding ticket.", e.getMessage());
    }

    /** todo: test for exception thrown if id of car exists and is 'in use' */
  }

  @Test
  /**
   * causes the current usage record of the season ticket associated with ticketID to be finalized.
   * throws throws a RuntimeException if the season ticket associated with ticketId does not exist,
   * or is not currently in use
   */
  void recordSeasonTicketExit() {


    logger.log(Level.INFO, "Testing recordSeasonTicketExit...");

    ISeasonTicket tkt = mock(SeasonTicket.class);

    when(tkt.getId()).thenReturn("S2222");

    when(tkt.getCarparkId()).thenReturn("Alphabet Street");

    testItem.registerSeasonTicket(tkt);

    testItem.recordSeasonTicketEntry(tkt.getId());

    testItem.recordSeasonTicketExit(tkt.getId());

    verify(seasonTicketDAO).recordTicketExit(tkt.getId());

  }

}