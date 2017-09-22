package bcccp.carpark.exit;

import bcccp.carpark.*;
import bcccp.tickets.adhoc.*;
import bcccp.tickets.season.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IntegExitControllerTest {

  static Carpark carpark;

  static ExitController sut;

  static IGate exitGate;

  static ICarSensor outsideSensor;

  static ICarSensor insideSensor;

  static IExitUI exitUserInterface;

  static IAdhocTicketDAO adhocTicketDAO;

  static AdhocTicketFactory adhocTicketFactory;

  static ISeasonTicketDAO seasonTicketDAO;

  static IUsageRecordFactory usageRecordFactory;

  static SeasonTicket seasonTicket;

  static IAdhocTicket adhocTicket;

  static String barcodeForAdhocTicket;

  static String idForSeasonTicket;

  static long validStartTime;

  static long validEndTime;


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

    exitGate = new Gate(20,20);

    outsideSensor = new CarSensor("Outside Sensor",20, 20);

    insideSensor = new CarSensor("Inside Sensor", 20, 20);

    adhocTicketFactory = new AdhocTicketFactory();

    adhocTicketDAO = new AdhocTicketDAO(adhocTicketFactory);

    exitUserInterface = new ExitUI(20,20);

    usageRecordFactory = new UsageRecordFactory();

    seasonTicketDAO = new SeasonTicketDAO(usageRecordFactory);

    carpark = new Carpark("Integral Carpark #12", 4500, adhocTicketDAO, seasonTicketDAO);
  }

  @BeforeEach
  void setUpEachTest() {

    sut = new ExitController(carpark, exitGate, insideSensor, outsideSensor, exitUserInterface);

    Calendar aValidStartTime = Calendar.getInstance();

    aValidStartTime.set(2017,9,20,9,52,38);

    validStartTime = aValidStartTime.getTimeInMillis();

    Calendar aValidEndTime = Calendar.getInstance();

    aValidEndTime.set(2017,9,28,11,43,12);

    validEndTime = aValidEndTime.getTimeInMillis();

    seasonTicket = new SeasonTicket("78", "Integral Carpark #12",
            validStartTime, validEndTime);

    seasonTicketDAO.registerTicket(seasonTicket);

    String testId = seasonTicket.getId();

    seasonTicketDAO.recordTicketEntry(seasonTicket.getId());

    adhocTicket = adhocTicketFactory.make("Integral Carpark #12", 51);

    barcodeForAdhocTicket = adhocTicketFactory.generateBarCode(51, "15052017111545");

    idForSeasonTicket = seasonTicket.getId();

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

      assertEquals("Invalid argument passed to Carpark constructor, entry gate arg.",
              e.getMessage());

    }

  }

  @Test
  void checkConstructorParamInSensorForNulls() {

    try {

      sut = new ExitController(carpark, exitGate, null, outsideSensor, exitUserInterface);

      fail("Expected: Should throw runtime exception for null as inside sensor arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, inside sensor arg.",
              e.getMessage());

    }

  }

  @Test
  void checkConstructorParamOutSensorForNulls() {

    try {

      sut = new ExitController(carpark, exitGate, insideSensor, null, exitUserInterface);

      fail("Expected: Should throw runtime exception for null as outside sensor arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, outside sensor arg.",
              e.getMessage());

    }

  }

  @Test
  void checkConstructorParamOutExitUIForNulls() {

    try {

      sut = new ExitController(carpark, exitGate, insideSensor, outsideSensor, null);
      fail("Expected: Should throw runtime exception for null as control pillar ui arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, control pillar ui arg.",
              e.getMessage());

    }

  }

  @Test
  void exitControllerRegisteredWithOutsideCarSensor() {

    assertEquals(outsideSensor.carIsDetected(),true);

  }

  @Test
  void exitControllerRegisteredWithInsideCarSensor() {

    assertEquals(insideSensor.carIsDetected(),true);

  }

  @Test
  void exitControllerInitialisedToIdle() {

    assertEquals(IDLE, sut.getState());

  }

  @Test
  void ticketInsertedCheckValidAdhocTicketProcessed() {

    adhocTicket.pay(new Date().getTime()+900000, 5.0f);

    sut.ticketInserted(barcodeForAdhocTicket);

    assertTrue((PROCESSED.equals(sut.getState())) &&
            (WAITING.equals(sut.getPrevState())));

  }

  @Test
  void ticketInsertedCheckNotValidAdhocTicketRejected() {

    sut.ticketInserted(barcodeForAdhocTicket);

    assertTrue((REJECTED.equals(sut.getState())) &&
            (WAITING.equals(sut.getPrevState())));

  }

  @Test
  void ticketInsertedCheckValidSeasonTicketProcessed() {

    sut.ticketInserted(idForSeasonTicket);

    assertTrue((PROCESSED.equals(sut.getState())) &&
            (WAITING.equals(sut.getPrevState())));

  }

  @Test
  void ticketInsertedCheckNotValidSeasonTicketRejected() {

    // Season ticket is not registered and does not have entry recorded, otherwise details are the same

    Calendar aValidStartTime = Calendar.getInstance();

    aValidStartTime.set(2017,9,20,9,52,38);

    validStartTime = aValidStartTime.getTimeInMillis();

    Calendar aValidEndTime = Calendar.getInstance();

    aValidEndTime.set(2017,9,28,11,43,12);

    validEndTime = aValidEndTime.getTimeInMillis();

    seasonTicket = new SeasonTicket("78", "Integral Carpark #12",
            validStartTime, validEndTime);

    sut.ticketInserted(idForSeasonTicket);

    assertTrue((REJECTED.equals(sut.getState())) &&
            (WAITING.equals(sut.getPrevState())));

  }

  @Test
  void ticketInsertedCheckNotValidTicket() {

    sut.ticketInserted("Invalid Ticket");

    assertTrue((REJECTED.equals(sut.getState())) &&
            (WAITING.equals(sut.getPrevState())));

  }

  @Test
  void ticketInsertedCheckNotWaitingState() {

    sut.ticketInserted(idForSeasonTicket);

    assertFalse(WAITING.equals(sut.getPrevState()));

  }

  @Test
  void ticketTakenProcessed() {

    sut.setStateFromString(PROCESSED);

    adhocTicket.exit(validEndTime);

    sut.ticketInserted(barcodeForAdhocTicket);

    sut.ticketTaken();

    assertEquals(TAKEN,sut.getState());

  }

  @Test
  void ticketTakenRejected() {

    sut.setStateFromString(REJECTED);

    sut.ticketTaken();

    assertEquals(WAITING,sut.getPrevState());

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarPresence() {

    sut.setStateFromString(IDLE);

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((WAITING.equals(sut.getState())) &&
            (IDLE.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresence() {

    sut.setStateFromString(IDLE);

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((BLOCKED.equals(sut.getState())) &&
            (IDLE.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenWaitingFullIssuedOrValidated() {

    // Note the spec for this event is incorrect - ExitController has no STATE for
    // FULL, ISSUED, or VALIDATED

    sut.setStateFromString(WAITING);

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue(IDLE.equals(sut.getState()));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresenceWhenWaitingFullIssuedOrValidated() {

    // Note the spec for this event is incorrect - ExitController has no STATE for
    // FULL, ISSUED, or VALIDATED

    sut.setStateFromString(WAITING);

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue(BLOCKED.equals(sut.getState()));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenBlocked() {

    sut.setStateFromString(BLOCKED);

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((IDLE.equals(sut.getState())) &&
            (BLOCKED.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenBlocked() {

    sut.setStateFromString(BLOCKED);

    sut.carEventDetected(outsideSensor.getId(), true);

    assertTrue(BLOCKED.equals(sut.getPrevState()));
  }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenTaken() {

    sut.setStateFromString(TAKEN);

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((IDLE.equals(sut.getState())) &&
              (TAKEN.equals(sut.getPrevState())));

    }

  @Test
  void carEventDetectedOutsideSensorDetectsCarPresenceWhenTaken() {

    sut.setStateFromString(TAKEN);

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((EXITING.equals(sut.getState())) &&
            (TAKEN.equals(sut.getPrevState())));

    }

  @Test
  void carEventDetectedInsideSensorDetectsCarAbsenceWhenExiting() {

    sut.setStateFromString(EXITING);

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((EXITED.equals(sut.getState())) &&
            (EXITING.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenExiting() {

    sut.setStateFromString(EXITING);

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((TAKEN.equals(sut.getState())) &&
            (EXITING.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedInsideSensorDetectsCarPresenceWhenExited() {

    sut.setStateFromString(EXITED);

    sut.carEventDetected(insideSensor.getId(),true);

    assertTrue((EXITING.equals(sut.getState())) &&
            (EXITED.equals(sut.getPrevState())));

  }

  @Test
  void carEventDetectedOutsideSensorDetectsCarAbsenceWhenExited() {

    sut.setStateFromString(EXITED);

    sut.carEventDetected(outsideSensor.getId(),true);

    assertTrue((IDLE.equals(sut.getState())) &&
            (EXITED.equals(sut.getPrevState())));

  }

}