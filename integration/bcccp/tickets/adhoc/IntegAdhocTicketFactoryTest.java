package bcccp.tickets.adhoc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class IntegAdhocTicketFactoryTest {
    static AdhocTicketFactory adhocTicketFactory;
    static IAdhocTicket iadhocTicket;
    static AdhocTicketDAO adhocTicketDAO;

    Logger logger = Logger.getLogger("Unit testing for AdhocTicketFactory class");

    @BeforeAll
    static void before(){
        adhocTicketFactory = mock(AdhocTicketFactory.class);
        iadhocTicket = mock(AdhocTicket.class);
    }

    @Test
    void testBarcodeGeneation() {
        logger.log(Level.INFO,"Test barcode generation");
        AdhocTicketFactory ad = new AdhocTicketFactory();
        IAdhocTicket ticket = ad.make("123",123);
        assertEquals(ad.generateBarCode(123,ad.entryDate()),ticket.getBarcode());
    }


    @Test
    void testMakeMethod(){
        logger.log(Level.INFO,"Test make method");
        when(adhocTicketFactory.make("123",123)).thenReturn(iadhocTicket);
        adhocTicketFactory.make("123",123);
        verify(adhocTicketFactory).make("123",123);
    }

}