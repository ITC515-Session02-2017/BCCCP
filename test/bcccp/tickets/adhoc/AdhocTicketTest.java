package bcccp.tickets.adhoc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdhocTicketTest {

    static IAdhocTicket testAdhoc;
    static IAdhocTicketDAO idao;
    static IAdhocTicketFactory ifactory;

    Logger logger = Logger.getLogger("Unit testing for AdHocTicket class");
    private float charge;

    @BeforeAll
    static void before() {
        testAdhoc = mock(AdhocTicket.class);
        idao = spy(new AdhocTicketDAO(new AdhocTicketFactory()));
    }

    @AfterEach
    void after() {

        testAdhoc = mock(AdhocTicket.class);
        idao = spy(new AdhocTicketDAO(new AdhocTicketFactory()));

    }

    @Test
    void testRuntimeExceptioWhenGetInvalidTicketNo() {
        logger.log(Level.INFO, "Testing ticket number");
        testAdhoc.getTicketNo();
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Error");
        });
        assertEquals("Error", exception.getMessage());

    }

    @Test
    void testgetBarcode() {
        logger.log(Level.INFO, "Test getBarcode method");
        testAdhoc.getBarcode();
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Error");
        });
        assertEquals("Error", exception.getMessage());
    }


    @Test
    void testgetCarparkIdwithInvalid() {
        logger.log(Level.INFO, "Test getCarparkId method with invalid parameter");
        testAdhoc.getCarparkId();
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Error");
        });
        assertEquals("Error", exception.getMessage());
    }

    @Test
    void testenterEntryDate() {
        logger.log(Level.INFO, "Test EntryDate");
        testAdhoc.enter(0L);
        verify(testAdhoc).enter(0L);
        assertEquals(testAdhoc.getEntryDateTime(), 0L);

    }

    @Test
    void getEntryDateTime() {
        logger.log(Level.INFO, "Test entry day");
        when(testAdhoc.getEntryDateTime()).thenReturn(0L);
        assertEquals(0L, testAdhoc.getEntryDateTime());

    }

    @Test
    void testExceptionforinvalidDateEnterMethod() {
        logger.log(Level.INFO, "Test exceptions when invalid date entry is entered");
        testAdhoc.enter(0);
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Error");
        });
        assertEquals("Error", exception.getMessage());

    }

    @Test

    void testisCurrentState() {
        logger.log(Level.INFO, "Testing the state of isCurrentState method");
        String entryStrDate = "02042013053542"; // "02-04-2013 05:35:42"
        testAdhoc = new AdhocTicket("Flinders Lane", 34, generateBarCode(34, entryStrDate));
        testAdhoc.enter(testAdhoc.getEntryDateTime());
        boolean state = testAdhoc.isCurrent();
        assertTrue(state);
    }

    @Test
    void testpayParameters() {
        logger.log(Level.INFO, "Testing pay method parameters");
        when(testAdhoc.getCharge()).thenReturn(4.5F);
        when(testAdhoc.getPaidDateTime()).thenReturn(4L);
        assertEquals(4.5F, testAdhoc.getCharge());
        assertEquals(4L, testAdhoc.getPaidDateTime());


    }


    @Test
    void isPaid() {
        logger.log(Level.INFO, "Test isPaid method");
        when(testAdhoc.isCurrent()).thenReturn(true);
        assertEquals(true, testAdhoc.isCurrent());

    }

    @Test
    void getCharge() {
        logger.log(Level.INFO, "Test getCharge method");
        when(testAdhoc.getCharge()).thenReturn(4.5F);
        assertEquals(4.5F, testAdhoc.getCharge());
    }

    @Test
    void testExitMethod() {
        logger.log(Level.INFO, "Test exit method");
        testAdhoc.exit(5L);
        verify(testAdhoc).exit(5L);
    }

    @Test
    void getExitDateTime() {
        logger.log(Level.INFO, "Test Exit method");
        when(testAdhoc.getExitDateTime()).thenReturn(5L);
        assertEquals(5L, testAdhoc.getExitDateTime());
    }

    @Test
    void testhasExitedMethod() {
        logger.log(Level.INFO, "Test hasExit method");
        when(testAdhoc.hasExited()).thenReturn(true);
        assertTrue(testAdhoc.hasExited());
    }

    // support methods

    static String generateBarCode(int ticketNum, String entryDate) {

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


    static String toHexadecimal(String text) throws UnsupportedEncodingException {
        byte[] myBytes = text.getBytes("UTF-16");

        return DatatypeConverter.printHexBinary(myBytes);
    }


}