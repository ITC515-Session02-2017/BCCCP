package bcccp.tickets.season;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class IntegSeasonTicketTest {

  static UsageRecordFactory usageRecordFactory;

  static SeasonTicket testTicket;

  static UsageRecord usageRecord;
  static ISeasonTicket iSeasonTicket;

  Logger logger = Logger.getLogger("Unit testing for SeasonTicket class");

  @BeforeAll
  public static void before() {

    testTicket = new SeasonTicket("S1234", "4", 0L, 1L);
    usageRecordFactory = new UsageRecordFactory();
    usageRecord = new UsageRecord("S1234", 0L);
    iSeasonTicket = mock(SeasonTicket.class);
  }

  @Test
  void getId() {
    logger.log(Level.INFO, "Testing getID method");
    assertEquals("S1234", testTicket.getId());
  }

  @Test
  void getCarparkId() {
    logger.log(Level.INFO, "Testing getCarparkID method");
    assertEquals("4", testTicket.getCarparkId());
  }

  @Test
  public void testSeasonTicketConstructorExceptions() {
    logger.log(Level.INFO, "Testing constructor parameters");

    SeasonTicket testTicket1 = new SeasonTicket(null, "M", 0L, 0L);
    SeasonTicket testTicket2 = new SeasonTicket("S1234", null, 0L, 0L);
    SeasonTicket testTicket3 = new SeasonTicket("S1234", "M", 0, 0L);
    SeasonTicket testTicket4 = new SeasonTicket("S1234", "M", 0L, 0);

    Throwable exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              throw new RuntimeException("Error");
            });
  }

  @Test
  void getStartValidPeriod() {
    logger.log(Level.INFO, "Testing getStartValidPeriod method");
    assertEquals(0L, testTicket.getStartValidPeriod());
  }

  @Test
  void getEndValidPeriod() {
    logger.log(Level.INFO, "Testing getEndValidPeriod method");
    assertEquals(1L, testTicket.getEndValidPeriod());
  }

  @Test
  void inUse() {
    logger.log(Level.INFO, "Testing inUse method");

    assertEquals(false, testTicket.inUse());
  }

  @Test
  void testRecordUsageExceptionWhenUsageRecordNull() {
    logger.log(Level.INFO, "Test exceptions when UsageRecord is null");
    IUsageRecord iu;
    ISeasonTicket it = mock(SeasonTicket.class);
    iu = null;
    doThrow(new RuntimeException()).when(it).recordUsage(iu);
  }

  @Test
  void getCurrentUsageRecord() {

    logger.log(Level.INFO, "Unit testing getCurrentUsageRecord method");
    assertEquals(testTicket.getId(), usageRecord.getSeasonTicketId());
  }

  @Test
  void endUsageExceptions() throws RuntimeException {
    logger.log(Level.INFO, "Test exception");

    ISeasonTicket se = mock(SeasonTicket.class);
    when(se.inUse()).thenReturn(false);
  }
}
