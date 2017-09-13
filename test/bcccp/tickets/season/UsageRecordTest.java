package bcccp.tickets.season;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class UsageRecordTest {

    static UsageRecord testUsage;
    static SeasonTicket testTicket;
    static IUsageRecord itestUsage;
    static UsageRecord record;

    Logger logger = Logger.getLogger("Unit Test for UsageRecord class");

    @BeforeAll
    static void before() {
        testUsage = mock(UsageRecord.class);
        testTicket = mock(SeasonTicket.class);
        itestUsage = mock(IUsageRecord.class);
        record = new UsageRecord("1234", 4L);

    }

    @Test
    void testUSageRecordParameters() {
        logger.log(Level.INFO, "Test USage Record parameters");
        assertEquals(record.getStartTime(), 4L);
        assertEquals(record.getSeasonTicketId(), "1234");

    }

    @Test
    void testFinaliseMethod() {
        logger.log(Level.INFO, "Test finalize method");
        testUsage.finalise(5L);
        verify(testUsage).finalise(5L);
    }

    @Test
    void testGetStartTimeReturn() {
        logger.log(Level.INFO, "Test to get the start time");
        assertEquals(4L, record.getStartTime());

    }

    @Test
    void testGetEndTimeReturn() {
        logger.log(Level.INFO, "Test to get the End time");
        record.finalise(5L);
        assertEquals(5L, record.getEndTime());

    }

    @Test
    void getSeasonTicketId() {
        logger.log(Level.INFO, "Test getSeasonTicketID method");
        assertEquals("1234", record.getSeasonTicketId());
    }

}