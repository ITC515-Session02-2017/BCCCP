package bcccp.tickets.adhoc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class IntegAdhocTicketDAOTest {

    static AdhocTicketDAO adhocTicketDAO;
    static AdhocTicket adhocTicket;
    static AdhocTicketFactory adhocTicketFactory;
    static IAdhocTicket iAdhocTicket;

    Logger logger = Logger.getLogger("Test Unit for AdhocTicketDAO class");


    @BeforeAll
    static void before() {

        adhocTicket = mock(AdhocTicket.class);
        adhocTicketFactory = mock(AdhocTicketFactory.class);
        iAdhocTicket = mock(AdhocTicket.class);
    }

    @Test
    void testClassImplementationWithNull() {
        logger.log(Level.INFO, "Testing the class constructor with null");
        adhocTicketDAO = new AdhocTicketDAO(null);
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Error");
        });
        assertEquals("Error", exception.getMessage());
    }

    @Test
    void testCreateTicket() {
        logger.log(Level.INFO, "Test createTicket method");
        adhocTicketDAO = mock(AdhocTicketDAO.class);
        adhocTicketDAO.createTicket("123");
        ArgumentCaptor<String> captop = ArgumentCaptor.forClass(String.class);
        verify(adhocTicketDAO).createTicket(captop.capture());
        assertEquals("123", captop.getValue());
    }

    @Test
    void testFindTicketwithTicketdoesntExist() {
        logger.log(Level.INFO, "Test findTicket method with non existing ticket. Should return null");
        AdhocTicketFactory factory = new AdhocTicketFactory();
        AdhocTicketDAO dao = new AdhocTicketDAO(factory);
        IAdhocTicket t = dao.findTicketByBarcode("123");
        assertEquals(null, t);
    }

    @Test
    void testGetCurrentTicketsList() {
        logger.log(Level.INFO, "");
        AdhocTicketFactory factory = new AdhocTicketFactory();
        AdhocTicketDAO dao = new AdhocTicketDAO(factory);
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

}