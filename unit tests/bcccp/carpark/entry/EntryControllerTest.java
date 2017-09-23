package bcccp.carpark.entry;

import bcccp.carpark.CarSensor;
import bcccp.carpark.Carpark;
import bcccp.carpark.Gate;
import bcccp.carpark.entry.EntryController;
import bcccp.carpark.entry.EntryUI;

import bcccp.tickets.adhoc.AdhocTicketDAO;
import bcccp.tickets.adhoc.IAdhocTicket;
import bcccp.tickets.adhoc.IAdhocTicketDAO;
import bcccp.tickets.season.ISeasonTicketDAO;
import bcccp.tickets.season.SeasonTicket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.BaseStubbing;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

public class EntryControllerTest {

  static Carpark carpark;

  static EntryController sut;

  static Gate entryGate;

  static CarSensor outsideSensor;

  static CarSensor insideSensor;

  static EntryUI entryUserInterface;

  static IAdhocTicketDAO adhocTicketDAOTest;

  static ISeasonTicketDAO seasonTicketDAOTest;

  static SeasonTicket seasonTicket;

  static String barcode;

  final String IDLE = "IDLE";

  final String WAITING = "WAITING";

  final String FULL = "FULL";

  final String VALIDATED = "VALIDATED";

  final String ISSUED = "ISSUED";

  final String TAKEN = "TAKEN";

  final String ENTERING = "ENTERING";

  final String ENTERED = "ENTERED";

  final String BLOCKED = "BLOCKED";

  @Captor
  private ArgumentCaptor <String> captor;

  @BeforeAll
  static void setupAllTests() {

    carpark = mock(Carpark.class);

    entryGate = mock(Gate.class);

    outsideSensor = mock(CarSensor.class);

    insideSensor = mock(CarSensor.class);

    entryUserInterface = mock(EntryUI.class);

    adhocTicketDAOTest = mock(AdhocTicketDAO.class);

    seasonTicket = mock(SeasonTicket.class);

  }

  @BeforeEach
  void setupEachTest() {

    when(insideSensor.getId()).thenReturn("Inside Sensor");

    when(outsideSensor.getId()).thenReturn("Outside Sensor");

    sut = new EntryController(carpark,entryGate,outsideSensor,insideSensor, entryUserInterface);

  }

  @AfterEach
  void cleanupEachTest() {

    sut = null;

  }

  @Test
  void checkConstructorParamCarparkForNulls() {

    try {

      sut = new EntryController(null, entryGate, outsideSensor, insideSensor, entryUserInterface);

      fail("Expected: Should throw runtime exception for null as carpark arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, carpark arg.", e.getMessage());

    }

  }

  @Test
  void checkConstructorParamGateForNulls() {

    try {

      sut = new EntryController(carpark, null, outsideSensor, insideSensor, entryUserInterface);

      fail("Expected: Should throw runtime exception for null as entry gate arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, entry gate arg.", e.getMessage());

    }

  }

  @Test
  void checkConstructorParamOutSensorForNulls() {

    try {

      sut = new EntryController(carpark, entryGate, null, insideSensor, entryUserInterface);

      fail("Expected: Should throw runtime exception for null as outside sensor arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, outside sensor arg.", e.getMessage());

    }

  }

  @Test
  void checkConstructorParamInSensorForNulls() {

    try {

      sut = new EntryController(carpark, entryGate, outsideSensor, null, entryUserInterface);

      fail("Expected: Should throw runtime exception for null as inside sensor arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, inside sensor arg.", e.getMessage());

    }

  }

  @Test
  void checkConstructorParamEntryUIForNulls() {

    try {

      sut = new EntryController(carpark, entryGate, outsideSensor, insideSensor, null);

      fail("Expected: Should throw runtime exception for null as entry user interface arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, entry user interface arg.", e.getMessage());

    }

  }

  @Test
  void entryControllerRegisteredWithCarpark() {

    verify(carpark).register(sut);

  }

  @Test
  void entryControllerRegisteredWithOutsideCarSensor() {

    verify(outsideSensor).registerResponder(sut);

  }

  @Test
  void entryControllerRegisteredWithInsideCarSensor() {

    verify(insideSensor).registerResponder(sut);

  }

  @Test
  void entryControllerRegisteredWithUserInterface() {

    verify(entryUserInterface).registerController(sut);

  }

  @Test
  void entryControllerInitialisedToIdle() {

    assertEquals(IDLE, sut.getState());

  }

  @Test
  void notifyCarparkEventCheckCarparkFull() {


    when(carpark.isFull()).thenReturn(false);

    sut.notifyCarparkEvent();

    verify(entryUserInterface).display("Push Button");

    assertTrue(WAITING.equals(sut.getState()));

  }

  @Test
  void buttonPushedCarparkFull() {

    when(carpark.isFull()).thenReturn(true);

    sut.buttonPushed();

    verify(entryUserInterface).display("Carpark Full");

    assertTrue(FULL.equals(sut.getState()) && WAITING.equals(sut.getPreviousState()));

  }

  @Test
  void buttonPushedCarparkNotFull() {

    when(carpark.isFull()).thenReturn(false);

    sut.buttonPushed();

    verify(entryUserInterface).display("Take Ticket");

    assertTrue(ISSUED.equals(sut.getState()) && WAITING.equals(sut.getPreviousState()));

  }

  @Test
  void buttonPushedNotWaiting() {

    sut.buttonPushed();

    verify(entryUserInterface).beep();

    assertNotEquals(WAITING, sut.getState());

  }

  @Test
  void ticketInsertedSeasonTicketValidAndNotInUse() {

    barcode = "Test Barcode";

    when(carpark.isSeasonTicketValid(barcode)).thenReturn(true);

    when(carpark.isSeasonTicketInUse(barcode)).thenReturn(false);

    sut.ticketInserted(barcode);

    verify(entryUserInterface).display("Take Ticket");

    assertTrue(VALIDATED.equals(sut.getState()) && WAITING.equals(sut.getPreviousState()));

  }

  @Test
  void ticketInsertedSeasonTicketNotValid() {

    barcode = "Test Barcode";

    when(carpark.isSeasonTicketValid(barcode)).thenReturn(false);

    when(carpark.isSeasonTicketInUse(barcode)).thenReturn(false);

    sut.ticketInserted(barcode);

    verify(entryUserInterface).beep();

    assertTrue(WAITING.equals(sut.getState()));

  }

  @Test
  void ticketInsertedSeasonTicketInUse() {

    barcode = "Test Barcode";

    when(carpark.isSeasonTicketValid(barcode)).thenReturn(true);

    when(carpark.isSeasonTicketInUse(barcode)).thenReturn(true);

    sut.ticketInserted(barcode);

    verify(entryUserInterface).beep();

    assertTrue(WAITING.equals(sut.getState()));

  }

  @Test
  void ticketInsertedNotWaiting() {

    barcode = "Test Barcode";

    sut.ticketInserted(barcode);

    verify(entryUserInterface).beep();

    assertFalse(WAITING.equals(sut.getState()));

  }

  @Test
  void ticketTakenIssuedOrValidated() {

    sut.ticketTaken();

    assertTrue((ISSUED.equals(sut.getPreviousState()) || VALIDATED.equals(sut.getPreviousState())) &&
            TAKEN.equals(sut.getState()));

  }

  @Test
  void ticketTakenNotIssuedOrValidated() {

    sut.ticketTaken();

    verify(entryUserInterface).beep();

    assertTrue((!ISSUED.equals(sut.getPreviousState()) && !VALIDATED.equals(sut.getPreviousState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresenceWhenIdle() {

    sut.carEventDetected(outsideSensor.getId(),true);

    verify(entryUserInterface).display("Push Button");

    assertTrue((WAITING.equals(sut.getState())) && IDLE.equals(sut.getPreviousState()));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarPresenceWhenIdle() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(BLOCKED.equals(sut.getState()) && IDLE.equals(sut.getPreviousState()));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenWaitingFullIssuedOrValidated() {

    boolean multiState = false;

    if (WAITING.equals(sut.getPreviousState()) || FULL.equals(sut.getPreviousState()) ||
            ISSUED.equals(sut.getPreviousState()) || VALIDATED.equals(sut.getPreviousState())) {

      multiState = true;

    }

    sut.carEventDetected(outsideSensor.getId(),false);

    assertTrue(IDLE.equals(sut.getState()) && multiState);

  }

  @Test

  void carEventDetectedInsideSensorDetectsCarPresenceWhenWaitingFullIssuedOrValidated() {

    boolean multiState = false;

    if (WAITING.equals(sut.getState()) || FULL.equals(sut.getState()) ||
            ISSUED.equals(sut.getState()) || VALIDATED.equals(sut.getState())) {

      multiState = true;

    }

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(BLOCKED.equals(sut.getState()) && !multiState);

  }
  @Test
  void carEventDetectedOutsideSensorDetectsCarPresenceWhenBlocked() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue(IDLE.equals(sut.getState()) && BLOCKED.equals(sut.getPreviousState()));

  }

  @Test

  void carEventDetectedInsideSensorDetectsCarPresenceWhenBlocked() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(BLOCKED.equals(sut.getState()));

  }

  @Test

  void carEventDetectedOutsideSensorDetectsCarPresenceWhenTicketTaken() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((IDLE.equals(sut.getState())) && TAKEN.equals(sut.getPreviousState()));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarPresenceWhenTicketTaken() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(ENTERING.equals(sut.getState()) && TAKEN.equals(sut.getPreviousState()));

  }

  @Test

  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenEntering() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue(ENTERED.equals(sut.getState()) && ENTERING.equals(sut.getPreviousState()));

  }

  @Test

  void carEventDetectedInsideSensorDetectsCarAbsenceWhenEntering() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(TAKEN.equals(sut.getState()) && ENTERING.equals(sut.getPreviousState()));

  }

  @Test

  void carEventDetectedOutsideSensorDetectsCarPresenceWhenEntered() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue(ENTERING.equals(sut.getState()) && ENTERED.equals(sut.getPreviousState()));

  }

  @Test

  void carEventDetectedInsideSensorDetectsCarPresenceWhenEntered() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(IDLE.equals(sut.getState()) && ENTERED.equals(sut.getPreviousState()));

  }

}