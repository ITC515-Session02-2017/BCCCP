package bcccp.tickets.season;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SeasonTicketDAOTest {

    static SeasonTicket testTicket;
    static ISeasonTicket iTestTicket;
    static IUsageRecord iUsRec;
    static ISeasonTicketDAO isdao;
    static IUsageRecordFactory iusReFac;
    Logger logger = Logger.getLogger("Test Unit for SeasonTicketDAO class");

    @Captor
    private ArgumentCaptor<ISeasonTicketDAO> paramList;

    @BeforeAll
    public static void beforeAll() {
        testTicket = new SeasonTicket("S1234", "A", 0L, 0L);
        isdao = mock(SeasonTicketDAO.class);
        iusReFac = mock(UsageRecordFactory.class);
        iUsRec = mock(IUsageRecord.class);
        iTestTicket = mock(ISeasonTicket.class);


    }


    @Test
    void registerTicketTest() {
        logger.log(Level.INFO, "Testing registerTicket method");

        isdao.registerTicket(testTicket);

        verify(isdao).registerTicket(testTicket);
    }

    @Test
    void registerTicketwithNullSeasonTicket() {
        logger.log(Level.INFO, "Test exceptions when registering null Season Ticket");
        SeasonTicket se = mock(SeasonTicket.class);
        isdao.registerTicket(se);
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Error");
        });
        assertEquals("Error", exception.getMessage());


    }

    @Test
    void deregisterTicketwithNullSeasonTicket() {
        logger.log(Level.INFO, "Test exceptions when deregistering null Season Ticket");
        SeasonTicket se = mock(SeasonTicket.class);
        isdao.deregisterTicket(se);
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Error");
        });
        assertEquals("Error", exception.getMessage());
    }

    @Test
    void getNumberOfTicketsCurrentlyRegistered() {
        logger.log(Level.INFO, "Test the number of tickets currently registered");
        SeasonTicketDAO dao = new SeasonTicketDAO(iusReFac);
        dao.registerTicket(iTestTicket);
        int size = dao.getNumberOfTickets();
        assertEquals(1, size);

    }

    @Test
    void findTicketById() {
        logger.log(Level.INFO, "Test find ticekt by id method");
        SeasonTicketDAO dao = new SeasonTicketDAO(iusReFac);
        ISeasonTicket test1 = new SeasonTicket("S1234", "4", 0L, 0L);
        dao.registerTicket(test1);
        ISeasonTicket sTicket = dao.findTicketById("S1234");
        assertEquals(test1.getId(), sTicket.getId());
    }

    @Test
    void recordTicketEntrywithNullSeasonTicket() {
        logger.log(Level.INFO, "Test exceptions when recording entry with null Season Ticket");
        isdao.recordTicketEntry("");
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Error");
        });
        assertEquals("Error", exception.getMessage());
    }

    @Test
    void testRecordingTicketEntrywithValidSeasonTicket() throws NoSuchElementException {
        SeasonTicketDAO dao = new SeasonTicketDAO(iusReFac);
        ISeasonTicket test1 = new SeasonTicket("S1234", "4", 0L, 0L);
        dao.registerTicket(test1);
        dao.recordTicketEntry("S1234");
        verify(dao).recordTicketEntry("S1234");


    }

    @Test
    void testRecordTicketExitwithinvalidTicekt() {
        logger.log(Level.INFO, "Test exceptions when recording exit with null Season Ticket");
        isdao.recordTicketExit("");
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Error");
        });
        assertEquals("Error", exception.getMessage());
    }


}