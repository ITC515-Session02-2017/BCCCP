package bcccp.carpark.exit;

import bcccp.carpark.ICarSensor;
import bcccp.carpark.Carpark;
import bcccp.carpark.IGate;

import bcccp.tickets.adhoc.AdhocTicket;
import bcccp.tickets.adhoc.AdhocTicketDAO;
import bcccp.tickets.adhoc.IAdhocTicketDAO;
import bcccp.tickets.season.SeasonTicket;
import bcccp.tickets.season.ISeasonTicketDAO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import static org.mockito.Matchers.anyString;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

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

  static ISeasonTicketDAO seasonTicketDAOTest;

  static SeasonTicket seasonTicket;

  static AdhocTicket adhocTicket;

  static String barcodeForAdhocTicket;

  static String barcodeForSeasonTicket;


  final String IDLE = "IDLE";

  final String WAITING = "WAITING";

  final String PROCESSED = "PROCESSED";

  final String REJECTED = "REJECTED";

  final String TAKEN = "TAKEN";

  final String EXITING = "EXITING";

  final String EXITED = "EXITED";

  final String BLOCKED = "BLOCKED";

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

    barcodeForAdhocTicket = "A valid Adhoc Ticket";

    barcodeForSeasonTicket = "S valid Season Ticket";

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

    assertEquals(IDLE, sut.getTestState());

  }

  @Test
  void ticketInsertedCheckProcessingValidAdhocTicket() {

    String validAdhocTicketBarcode = "A" + barcodeForAdhocTicket.substring(1);

    when(carpark.getAdhocTicket(validAdhocTicketBarcode)).thenReturn(adhocTicket);

    when(adhocTicket.getPaidDateTime()).thenReturn(new Date().getTime());

    when(adhocTicket.isPaid()).thenReturn(true);

    sut.ticketInserted(validAdhocTicketBarcode);

    assertTrue((PROCESSED.equals(sut.getTestState())) &&
            (WAITING.equals(sut.getPrevTestState())));

  }

  @Test
  void ticketInsertedCheckProcessingNotValidAdhocTicket() {

    String validAdhocTicketBarcode = "A" + barcodeForAdhocTicket.substring(1);

    when(carpark.getAdhocTicket(validAdhocTicketBarcode)).thenReturn(adhocTicket);

    when(adhocTicket.getPaidDateTime()).thenReturn(new Date().getTime());

    when(adhocTicket.isPaid()).thenReturn(false);

    sut.ticketInserted(validAdhocTicketBarcode);

    assertTrue((REJECTED.equals(sut.getTestState())) &&
            (WAITING.equals(sut.getPrevTestState())));

  }

  @Test
  void ticketInsertedCheckProcessingValidSeasonTicket() {

    String validSeasonTicketBarcode = "S" + barcodeForSeasonTicket.substring(1);

    when(carpark.isSeasonTicketValid(validSeasonTicketBarcode)).thenReturn(true);

    when(carpark.isSeasonTicketInUse(validSeasonTicketBarcode)).thenReturn(true);

    sut.ticketInserted(validSeasonTicketBarcode);

    assertTrue((PROCESSED.equals(sut.getTestState())) &&
            (WAITING.equals(sut.getPrevTestState())));

  }

  @Test
  void ticketInsertedCheckProcessingNotValidSeasonTicket() {

    String validSeasonTicketBarcode = "S" + barcodeForSeasonTicket.substring(1);

    when(carpark.isSeasonTicketValid(validSeasonTicketBarcode)).thenReturn(false);

    when(carpark.isSeasonTicketInUse(validSeasonTicketBarcode)).thenReturn(false);

    sut.ticketInserted(barcodeForSeasonTicket);

    assertTrue((REJECTED.equals(sut.getTestState())) &&
            (WAITING.equals(sut.getPrevTestState()))); }


  @Test
  void ticketInsertedCheckProcessingNotValidTicket() {

    sut.ticketInserted("Invalid Ticket");

    assertTrue((REJECTED.equals(sut.getTestState())) &&
            (WAITING.equals(sut.getPrevTestState())));

    verify(exitUserInterface).beep();

  }

  @Test
  void ticketInsertedCheckProcessingNotWaitingState() {

    sut.ticketInserted(barcodeForSeasonTicket);

    verify(exitUserInterface).beep();

    assertFalse(WAITING.equals(sut.getPrevTestState()));

  }

  @Test
  void ticketTakenProcessed() {

    sut.ticketTaken();

    assertTrue((TAKEN.equals(sut.getTestState())) &&
            (PROCESSED.equals(sut.getPrevTestState())));

  }

  @Test
  void ticketTakenRejected() {

    sut.ticketTaken();

    assertTrue((WAITING.equals(sut.getTestState())) &&
            (REJECTED.equals(sut.getPrevTestState())));

  }

  @Test
  void ticketTakenBeep() {

    sut.ticketTaken();

    verify(exitUserInterface).beep();

    assertFalse((PROCESSED.equals(sut.getPrevTestState())) ||
            (REJECTED.equals(sut.getPrevTestState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarPresence() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((WAITING.equals(sut.getTestState())) &&
            (IDLE.equals(sut.getPrevTestState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresence() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((BLOCKED.equals(sut.getTestState())) &&
            (IDLE.equals(sut.getPrevTestState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenWaitingFullIssuedOrValidated() {

    // Note the spec for this event is incorrect - ExitController has no STATE for FULL, ISSUED, or VALIDATED

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(IDLE.equals(sut.getTestState()));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresenceWhenWaitingFullIssuedOrValidated() {

    // Note the spec for this event is incorrect - ExitController has no STATE for FULL, ISSUED, or VALIDATED

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue(BLOCKED.equals(sut.getTestState()));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenBlocked() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((IDLE.equals(sut.getTestState())) &&
            (BLOCKED.equals(sut.getPrevTestState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenBlocked() {

    sut.carEventDetected(outsideSensor.getId(), true);

    assertTrue(BLOCKED.equals(sut.getPrevTestState()));
  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenTaken() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((IDLE.equals(sut.getTestState())) &&
              (TAKEN.equals(sut.getPrevTestState())));

    }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresenceWhenTaken() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((EXITING.equals(sut.getTestState())) &&
            (TAKEN.equals(sut.getPrevTestState())));

    }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenExiting() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((EXITED.equals(sut.getTestState())) &&
            (EXITING.equals(sut.getPrevTestState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenExiting() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((TAKEN.equals(sut.getTestState())) &&
            (EXITING.equals(sut.getPrevTestState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenExited() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((EXITING.equals(sut.getTestState())) &&
            (EXITED.equals(sut.getPrevTestState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenExited() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((IDLE.equals(sut.getTestState())) &&
            (EXITED.equals(sut.getPrevTestState())));

  }

}