package bcccp.carpark.entry;

import bcccp.carpark.CarSensor;
import bcccp.carpark.Carpark;
import bcccp.carpark.Gate;
import bcccp.carpark.ICarSensor;
import bcccp.carpark.IGate;
import bcccp.carpark.entry.EntryController;
import bcccp.carpark.entry.EntryUI;
import bcccp.carpark.entry.IEntryUI;
import bcccp.tickets.adhoc.AdhocTicketDAO;
import bcccp.tickets.season.ISeasonTicket;
import bcccp.tickets.season.SeasonTicket;
import bcccp.tickets.season.SeasonTicketDAO;
import bcccp.tickets.season.UsageRecordFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class IntegrationEntryControllerwithSeasonTicket {

    static ISeasonTicket testSeason;
    static Carpark testCarpark;
    static IEntryUI guiEntry;
    static ICarSensor outSideSensor;
    static ICarSensor insideSensor;
    static EntryController entryController;
    static IGate testGate;
    static AdhocTicketDAO adhocTicketDAO;
    static SeasonTicketDAO seasonTicketDAO;

    static int takenSpaces;

    Logger logger = Logger.getLogger("High Level Integration test for Case Scenario EntryController");

    @BeforeAll
    static void before() throws Exception {

        seasonTicketDAO = new SeasonTicketDAO(new UsageRecordFactory());
        guiEntry = mock(EntryUI.class);
        testCarpark = new Carpark("TestCarPark", 1, adhocTicketDAO, seasonTicketDAO);
        testSeason = new SeasonTicket("1234", "Carpark A", 1L, 3L);
        outSideSensor = new CarSensor("Outgoing Car", 0, 0);
        insideSensor = new CarSensor("Incoming Car", 0, 0);
        testGate = new Gate(1, 1);
        entryController = new EntryController(testCarpark, testGate, outSideSensor, insideSensor, guiEntry);
        outSideSensor.registerResponder(entryController);
        insideSensor.registerResponder(entryController);
        testCarpark.register(entryController);
        guiEntry.registerController(entryController);
    }


    @Test
    public void testIncomingCarToCarparkwithSeasonTicket() {
        logger.log(Level.INFO, "Testing incoming car into carpar. Season ticket");
        assertEquals(entryController.getState().toString(), "IDLE");
        takenSpaces = testCarpark.getNumberOfCarsParked();
        outSideSensor.sensorOposite();
        entryController.carEventDetected(outSideSensor.getId(), true);
        assertEquals(entryController.getState().toString(), "WAITING");
        verify(guiEntry).display((contains("Push Button")));
        seasonTicketDAO.registerTicket(testSeason);
        testCarpark.recordSeasonTicketEntry("1234");
        assertEquals(testSeason, seasonTicketDAO.findTicketById("1234"));
        testGate.raise();
        assertTrue(testGate.isRaised());
        entryController.notifyCarparkEvent();
        assertEquals(entryController.getState().toString(), "WAITING");
        testGate.lower();
        assertFalse(testGate.isRaised());
    }

    /*TEST FAIL: Due to IDLE and WAITING select case needs fix in EntryController class*/
    @Test
    public void testIncomingCarNoEntryDetection() {
        logger.log(Level.INFO,"Testing incoming car but no entry detection. The car leave");
        assertEquals(entryController.getState().toString(), "IDLE");
        outSideSensor.sensorOposite();
        entryController.carEventDetected(outSideSensor.getId(), true);

        assertEquals(entryController.getState().toString(), "WAITING");
        verify(guiEntry).display((contains("Push Button")));

        outSideSensor.sensorOposite();
        entryController.carEventDetected(outSideSensor.getId(), false);
        assertEquals(entryController.getState().toString(), "IDLE");

    }

    @Test
    public void testIncomingCarwithInvalidSeasonTicket() throws NullPointerException {
        logger.log(Level.INFO, "Testing inserting and invalid season ticket");
        assertEquals(entryController.getState().toString(), "IDLE");
        takenSpaces = testCarpark.getNumberOfCarsParked();
        outSideSensor.sensorOposite();
        entryController.carEventDetected(outSideSensor.getId(), true);
        assertEquals(entryController.getState().toString(), "WAITING");
        verify(guiEntry).display((contains("Push Button")));

        seasonTicketDAO.registerTicket(testSeason);

        assertEquals(entryController.getState().toString(), "WAITING");
        entryController.ticketTaken();
        verify(guiEntry).display((contains("Push Button")));
    }


    @Test
    public void testIncomingCartoFullCarparkSeasonTicket() {
        logger.log(Level.INFO, "Test full carpark");
        testCarpark.recordAdhocTicketEntry();
        assertEquals(entryController.getState().toString(), "IDLE");
        outSideSensor.sensorOposite();
        entryController.carEventDetected(outSideSensor.getId(), true);
        assertEquals(entryController.getState().toString(), "WAITING");
        verify(guiEntry).display((contains("Push Button")));
        entryController.buttonPushed();
        assertEquals(testCarpark.isFull(), true);
        outSideSensor.sensorOposite();
        entryController.carEventDetected(outSideSensor.getId(), false);
        assertEquals(entryController.getState().toString(), "FULL");
    }

}

