package bcccp.carpark;

import bcccp.carpark.entry.EntryController;
import bcccp.carpark.entry.EntryUI;
import bcccp.tickets.adhoc.*;
import bcccp.tickets.season.*;

import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

//RunWith(MockitoJUnitRunner.class)
class CarparkTest {

    static IAdhocTicketDAO adhocTicketDAO;

    static ISeasonTicketDAO seasonTicketDAO;

    static Carpark cp;

    static Carpark testItem;

    static EntryController entryController;

    static String DEFAULT_CARPARK = "Alphabet Street";

    static int DEFAULT_CAPACITY = 3;

    Logger logger = Logger.getLogger("Unit testing for Carpark class.");


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

            assertEquals("Invalid argument passed to Carpark constructor.", e.getMessage());
        }

        // invalid 'name' argument: empty
        try {

            Carpark testitem = new Carpark("", 3, adhocTicketDAO, seasonTicketDAO);

            fail("Expected a RuntimeException to be thrown");

        } catch (Exception e) {

            assertEquals("Invalid argument passed to Carpark constructor.", e.getMessage());
        }

        // invalid 'capacity' argument: empty
        try {

            Carpark testItem = new Carpark("Bathurst Chase", 0, adhocTicketDAO, seasonTicketDAO);

            fail("Expected a RuntimeException to be thrown");

        } catch (Exception e) {

            assertEquals("Invalid argument passed to Carpark constructor.", e.getMessage());
        }

        // invalid 'capacity' argument: negative
        try {

            Carpark testItem = new Carpark("Bathurst Chase", -1, adhocTicketDAO, seasonTicketDAO);

            fail("Expected a RuntimeException to be thrown");

        } catch (Exception e) {

            assertEquals("Invalid argument passed to Carpark constructor.", e.getMessage());
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
     * The ‘Out-of-Hours’ rate of $2/hr should be charged for all the time the car was parked outside <br />
     * of business hours. The ‘Business-Hours’ rate of $5/hr should be charged for all the time the <br />
     * car was parked during business hours. Business hours are defined as between 7AM and 7PM, Monday <br />
     * to Friday Parking charges are calculated in minute increments. <br />
     *
     * <p>NOTE: following test is necessarily 'white box'</p>
     */
    void calculateAddHocTicketCharge() {

        logger.log(Level.INFO, "Testing calculateAdHocTicketCharge...");

        float WORKING_HRS_RATE =
                5.0f; //'rates' in public float calculateAddHocTicketCharge(long entryDateTime)

        float OUT_OF_HRS_RATE = 2.0f;   //not yet defined in public float calculateAddHocTicketCharge(long entryDateTime)

        float chargeAmount;

        Date entryDate = null;

        Date exitDate = null;

        String entryStrDate, exitStrDate;

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
        // formula tested below is from:  public float calculateAddHocTicketCharge(long entryDateTime)
        chargeAmount = (exitDate.getTime() - entryDate.getTime()) * WORKING_HRS_RATE / 60000;

        // five dollars an hour for one hour = 5 dollars (at "working hours" rate)
        assertEquals(5.0, chargeAmount);

        /** ********************************************************************* */
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

        ticket = new AdhocTicket(testItem.getName(), 2, generateBarCode(2, entryStrDate));

        ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");

        logger.log(Level.INFO, "Equivalence partition for: OUT OF HOURS ");

        logger.log(Level.INFO, "Entry Date: " + ft.format(ticket.getEntryDateTime()));

        logger.log(
                Level.INFO,
                "Checkout Date: " + ft.format(ticket.getEntryDateTime() + TimeUnit.HOURS.toMillis(2)));
        // formula tested below is from:  public float calculateAddHocTicketCharge(long entryDateTime)
        chargeAmount = (exitDate.getTime() - entryDate.getTime()) * OUT_OF_HRS_RATE / 60000;

        // two dollars an hour for two hour = 4 dollars (at "out of hours" rate)
        assertEquals(4.0, chargeAmount);

        /** ********************************************************************* */


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

        ticket = new AdhocTicket(testItem.getName(), 3, generateBarCode(3, entryStrDate));

        ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");

        logger.log(Level.INFO, "Boundary test for: OUT OF HOURS and WORKING HOURS");

        logger.log(Level.INFO, "Entry Date: " + ft.format(ticket.getEntryDateTime()));

        logger.log(
                Level.INFO,
                "Checkout Date: " + ft.format(ticket.getEntryDateTime() + TimeUnit.HOURS.toMillis(2)));
        // formula tested below is from:  public float calculateAddHocTicketCharge(long entryDateTime)
        chargeAmount = (exitDate.getTime() - entryDate.getTime()) * WORKING_HRS_RATE / 60000;

        // two dollars an hour for two hour = 4 dollars (at "out of hours" rate)
        // plus five dollars an hour for two hours = $14
        assertEquals(14.0, chargeAmount);
    }

    @Test
    /**
     * registers a season ticket with the carpark so that the season ticket may be used to access the
     * carpark throws a RuntimeException if the carpark the season ticket is associated is not the
     * same as the carpark name.
     */
    void registerSeasonTicket() {

        logger.log(Level.INFO, "Testing registerSeasonTicket...");


        ISeasonTicket tktA = mock(SeasonTicket.class);

        doReturn("S2222").when(tktA).getId();

        testItem.registerSeasonTicket(tktA);

        assertEquals(true, testItem.isSeasonTicketInUse("S2222"));


        try {

            testItem.registerSeasonTicket(new SeasonTicket("S9999", "Wrong Name", 0L, 0L));

            fail("Expected a RuntimeException to be thrown");

        } catch (Exception e) {

            assertEquals("Wrong carpark!", e.getMessage());
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

        testItem.registerSeasonTicket(tkt);

        testItem.deregisterSeasonTicket(tkt);

        verify(seasonTicketDAO).deregisterTicket(tkt);

    }

    @Test
    void isSeasonTicketValid() {

        //not tested (valid if other methods are tested and passing)
    }

    @Test
    void isSeasonTicketInUse() {


        logger.log(Level.INFO, "Testing isSeasonTicketInUse...");

        ISeasonTicket tkt = mock(SeasonTicket.class);

        when(tkt.getId()).thenReturn("S2222");

        testItem.registerSeasonTicket(tkt);

        assertEquals(true, testItem.isSeasonTicketInUse(tkt.getId()));
    }

    @Test
    /**
     * causes a new usage record to be created and associated with a season ticket Throws a
     * RuntimeException if the season ticket associated with ticketId does not exist, or is currently
     * in use
     */
    void recordSeasonTicketEntry() {


        logger.log(Level.INFO, "Testing recordSeasonTicketEntry...");

        ISeasonTicket tkt = mock(SeasonTicket.class);

        when(tkt.getId()).thenReturn("S2222");

        testItem.registerSeasonTicket(tkt);

        testItem.recordSeasonTicketEntry(tkt.getId());

        verify(seasonTicketDAO).recordTicketEntry("S2222");


        try {
            //no ticket has the following id:
            testItem.recordSeasonTicketEntry("badId");

            fail("Expected a RuntimeException to be thrown");

        } catch (Exception e) {

            assertEquals("No ticket found!", e.getMessage());
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

        testItem.registerSeasonTicket(tkt);

        testItem.recordSeasonTicketEntry(tkt.getId());

        testItem.recordSeasonTicketExit(tkt.getId());

        verify(seasonTicketDAO).recordTicketExit(tkt.getId());

    }

    public String generateBarCode(int ticketNum, String entryDate) {

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


    public static String toHexadecimal(String text) throws UnsupportedEncodingException {
        byte[] myBytes = text.getBytes("UTF-16");

        return DatatypeConverter.printHexBinary(myBytes);
    }
}
