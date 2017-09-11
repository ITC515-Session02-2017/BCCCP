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

class EntryControllerTest {

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

  private enum STATE {
    IDLE,
    WAITING,
    FULL,
    VALIDATED,
    ISSUED,
    TAKEN,
    ENTERING,
    ENTERED,
    BLOCKED}

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

    // NOTE: this test will not compile when getter method for STATE is removed from EntryController
    assertEquals(STATE.IDLE, sut.getState());

  }

  @Test
  void buttonPushedNotWaiting() {

    sut.buttonPushed();

    assertNotEquals(STATE.WAITING, sut.getState());

  }

  @Test
  void ticketInsertedWaitingIsSeasonTicketValidNotInUse() {

    barcode = "Test Barcode";

    sut.ticketInserted(barcode);

    when(carpark.isSeasonTicketValid(barcode) && !carpark.isSeasonTicketInUse(barcode)).thenReturn(true);

  }

  @Test
  void ticketTakenIfIssuedOrValidated() {

    sut.ticketTaken();

    assertEquals((STATE.ISSUED.equals(sut.getPreviousState())) || (STATE.VALIDATED.equals(sut.getPreviousState())),
            STATE.TAKEN.equals(sut.getState()));

  }

  @Test
  void notifyCarparkEventCheckAdhocSpacesAvailable() {

    sut.notifyCarparkEvent();

    assertTrue((STATE.WAITING.equals(sut.getState())) && !carpark.isFull());

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresenceWhenIdle() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((STATE.WAITING.equals(sut.getState())) && (STATE.IDLE.equals(sut.getPreviousState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarPresenceWhenIdle() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(STATE.BLOCKED.equals(sut.getState()) && (STATE.IDLE.equals(sut.getPreviousState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenWaitingFullIssuedOrValidated() {

    boolean multiState = false;

    if (STATE.WAITING.equals(sut.getPreviousState()) || STATE.FULL.equals(sut.getPreviousState()) ||
            STATE.ISSUED.equals(sut.getPreviousState()) || STATE.VALIDATED.equals(sut.getPreviousState())) {

      multiState = true;

    }

    sut.carEventDetected(outsideSensor.getId(),false);

    assertTrue(STATE.IDLE.equals(sut.getState()) && multiState);

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenWaitingFullIssuedOrValidated() {

    boolean multiState = false;

    if (STATE.WAITING.equals(sut.getState()) || STATE.FULL.equals(sut.getState()) ||
            STATE.ISSUED.equals(sut.getState()) || STATE.VALIDATED.equals(sut.getState())) {

      multiState = true;

    }

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(STATE.BLOCKED.equals(sut.getState()) && !multiState);

  }
  @Test
  void carEventDetectedOutsideSensorDetectsCarPresenceWhenBlocked() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((STATE.IDLE.equals(sut.getState())) && (STATE.BLOCKED.equals(sut.getPreviousState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarPresenceWhenBlocked() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(STATE.BLOCKED.equals(sut.getState()));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresenceWhenTicketTaken() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((STATE.IDLE.equals(sut.getState())) && (STATE.TAKEN.equals(sut.getPreviousState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarPresenceWhenTicketTaken() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(STATE.ENTERING.equals(sut.getState()) && (STATE.TAKEN.equals(sut.getPreviousState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresenceWhenEntering() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((STATE.ENTERED.equals(sut.getState())) && (STATE.ENTERING.equals(sut.getPreviousState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarPresenceWhenEntering() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(STATE.TAKEN.equals(sut.getState()) && (STATE.ENTERING.equals(sut.getPreviousState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresenceWhenEntered() {

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((STATE.ENTERING.equals(sut.getState())) && (STATE.ENTERED.equals(sut.getPreviousState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarPresenceWhenEntered() {

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(STATE.IDLE.equals(sut.getState()) && (STATE.ENTERED.equals(sut.getPreviousState())));

  }

}