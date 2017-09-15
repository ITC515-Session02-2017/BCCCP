package bcccp.carpark.exit;

import bcccp.carpark.ICarSensor;
import bcccp.carpark.Carpark;
import bcccp.carpark.IGate;

import bcccp.tickets.adhoc.AdhocTicket;
import bcccp.tickets.adhoc.AdhocTicketDAO;
import bcccp.tickets.adhoc.IAdhocTicketDAO;
import bcccp.tickets.season.SeasonTicket;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExitControllerTest {

  static Carpark carpark;

  static ExitController sut;

  static IGate exitGate;

  static ICarSensor outsideSensor;

  static ICarSensor insideSensor;

  static IExitUI exitUserInterface;

  static IAdhocTicketDAO adhocTicketDAOTest;

  static SeasonTicket seasonTicket;

  static AdhocTicket adhocTicket;

  static String barcodeForAdhocTicket;

  static String barcodeForSeasonTicket;

  private enum STATE {
    IDLE,
    WAITING,
    PROCESSED,
    REJECTED,
    TAKEN,
    EXITING,
    EXITED,
    BLOCKED}

  @BeforeAll
  static void setupAllTests() {

    carpark = mock(Carpark.class);

    exitGate = mock(IGate.class);

    outsideSensor = mock(ICarSensor.class);

    insideSensor = mock(ICarSensor.class);

    exitUserInterface = mock(IExitUI.class);

    adhocTicketDAOTest = mock(AdhocTicketDAO.class);

    seasonTicket = mock(SeasonTicket.class);

    adhocTicket = mock(AdhocTicket.class);

  }

  @BeforeEach
  void setUpEachTest() {

    sut = new ExitController(carpark, exitGate, insideSensor, outsideSensor, exitUserInterface);

    barcodeForAdhocTicket = "Valid Adhoc Ticket";

    barcodeForSeasonTicket = "Valid Season Ticket";

    when(insideSensor.getId()).thenReturn("Inside Sensor");

    when(outsideSensor.getId()).thenReturn("Outside Sensor");

  }

  @Test
  void checkConstructorParamCarparkForNulls() {

    try {

      sut = new ExitController(null, exitGate, insideSensor, outsideSensor, exitUserInterface);

      fail("Expected: Should throw runtime exception for null as carpark arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, carpark arg.", e.getMessage());

    }

  }

  @Test
  void checkConstructorParamGateForNulls() {

    try {

      sut = new ExitController(carpark, null, insideSensor, outsideSensor, exitUserInterface);

      fail("Expected: Should throw runtime exception for null as entry gate arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, entry gate arg.", e.getMessage());

    }

  }

  @Test
  void checkConstructorParamInSensorForNulls() {

    try {

      sut = new ExitController(carpark, exitGate, null, outsideSensor, exitUserInterface);

      fail("Expected: Should throw runtime exception for null as inside sensor arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, inside sensor arg.", e.getMessage());

    }

  }

  @Test
  void checkConstructorParamOutSensorForNulls() {

    try {

      sut = new ExitController(carpark, exitGate, insideSensor, null, exitUserInterface);

      fail("Expected: Should throw runtime exception for null as outside sensor arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, outside sensor arg.", e.getMessage());

    }

  }

  @Test
  void checkConstructorParamOutExitUIForNulls() {

    try {

      sut = new ExitController(carpark, exitGate, insideSensor, outsideSensor, null);
      fail("Expected: Should throw runtime exception for null as control pillar ui arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, control pillar ui arg.", e.getMessage());

    }

  }

  @Test
  void exitControllerRegisteredWithOutsideCarSensor() {

    verify(outsideSensor).registerResponder(sut);

  }

  @Test
  void exitControllerRegisteredWithInsideCarSensor() {

    verify(insideSensor).registerResponder(sut);

  }

  @Test
  void exitControllerRegisteredWithUserInterface() {

    verify(exitUserInterface).registerController(sut);

  }

  @Test
  void exitControllerInitialisedToIdle() {

    assertEquals(STATE.IDLE, sut.getState());

  }

  @Test
  void ticketInsertedCheckProcessingValidAdhocTicket() {

    sut.ticketInserted(barcodeForAdhocTicket);

    assertTrue((STATE.PROCESSED.equals(sut.getState())) &&
            (STATE.WAITING.equals(sut.getPrevState())));

  }

  @Test
  void ticketInsertedCheckProcessingValidSeasonTicket() {

    sut.ticketInserted(barcodeForSeasonTicket);

    assertTrue((STATE.PROCESSED.equals(sut.getState())) &&
            (STATE.WAITING.equals(sut.getPrevState())));

  }

  @Test
  void ticketInsertedCheckProcessingNotValidTicket() {

    sut.ticketInserted("Invalid Ticket");

    assertTrue((STATE.REJECTED.equals(sut.getState())) &&
            (STATE.WAITING.equals(sut.getPrevState())));

  }

  @Test
  void ticketTakenProcessed() {

    sut.ticketTaken();

    assertTrue((STATE.TAKEN.equals(sut.getState())) &&
            (STATE.PROCESSED.equals(sut.getPrevState())));

  }

  @Test
  void ticketTakenRejected() {

    sut.ticketTaken();

    assertTrue((STATE.WAITING.equals(sut.getState())) &&
            (STATE.REJECTED.equals(sut.getPrevState())));

  }

  @Test
  void ticketTakenBeep() {

    sut.ticketTaken();

    verify(exitUserInterface).beep();

    assertFalse((STATE.PROCESSED.equals(sut.getPrevState())) ||
            (STATE.REJECTED.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarPresence() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((STATE.IDLE.equals(sut.getState())) &&
            (STATE.WAITING.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresence() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((STATE.IDLE.equals(sut.getState())) &&
            (STATE.BLOCKED.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenWaitingFullIssuedOrValidated() {

    // Note the spec for this event is incorrect - ExitController has no STATE for FULL, ISSUED, or VALIDATED

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(STATE.IDLE.equals(sut.getState()) &&
            (STATE.WAITING.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenWaitingFullIssuedOrValidated() {

    // Note the spec for this event is incorrect - ExitController has no STATE for FULL, ISSUED, or VALIDATED

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue(STATE.BLOCKED.equals(sut.getState()) &&
            (STATE.WAITING.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenBlocked() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((STATE.IDLE.equals(sut.getState())) &&
            (STATE.BLOCKED.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenBlocked() {

    sut.carEventDetected(outsideSensor.getId(), true);

    assertTrue(STATE.BLOCKED.equals(sut.getPrevState()));
  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenTaken() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((STATE.IDLE.equals(sut.getState())) &&
              (STATE.TAKEN.equals(sut.getPrevState())));

    }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenTaken() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((STATE.EXITING.equals(sut.getState())) &&
            (STATE.TAKEN.equals(sut.getPrevState())));

    }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenExiting() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((STATE.EXITED.equals(sut.getState())) &&
            (STATE.EXITING.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenExiting() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((STATE.TAKEN.equals(sut.getState())) &&
            (STATE.EXITING.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenExited() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((STATE.EXITING.equals(sut.getState())) &&
            (STATE.EXITED.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenExited() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((STATE.IDLE.equals(sut.getState())) &&
            (STATE.EXITED.equals(sut.getPrevState())));

  }

}