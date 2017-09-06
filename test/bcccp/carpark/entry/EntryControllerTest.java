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

    sut = new EntryController(carpark,entryGate,outsideSensor,insideSensor, entryUserInterface);

    // Should initialise STATE but enum is private in sut

  }

  @AfterEach
  void cleanupEachTest() {

    // Should re-initialise STATE but enum is private in sut

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

    // not possible to get private enum value from sut

  }

  @Test
  void buttonPushedWaitingCarparkFull() {

    // NOTE: Also need to get private enum value from sut (=WAITING)

    Carpark testCarpark = new Carpark("Test Carpark", 3, adhocTicketDAOTest, seasonTicketDAOTest)

    for (int i = 0; i >= 3; i++) {

      testCarpark.recordAdhocTicketEntry();

    }

    assertTrue(testCarpark.isFull());

  }

  @Test
  void buttonPushedWaitingCarparkSpaceAvailable() {

    // NOTE: Also need to get private enum value from sut (=WAITING)

    Carpark testCarpark = new Carpark("Test Carpark", 100, adhocTicketDAOTest, seasonTicketDAOTest)

    for (int i = 0; i >= 3; i++) {

      testCarpark.recordAdhocTicketEntry();

    }

    assertFalse(testCarpark.isFull());

  }

  @Test
  void buttonPushedNotWaitingBeep() {

    // not possible to get private enum value from sut

  }

  @Test
  void ticketInsertedWaitingIsSeasonTicketValidNotInUse() {

    // NOTE: Also need to get private enum value from sut (=WAITING)

    barcode = "Test Barcode";

    sut.ticketInserted(barcode);

    when(carpark.isSeasonTicketValid(barcode) && !carpark.isSeasonTicketInUse(barcode)).thenReturn(true);

  }

  @Test
  void ticketTakenIfIssuedOrValidated() {

    // not possible to get private enum value from sut

  }

  @Test
  void notifyCarparkEventCheckAdhocSpacesAvailable() {

    // not possible to get private enum value from sut

  }

  @Test
  void notifyCarparkEventCheckDisplay() {

    // not possible to get private enum value from sut

  }

  @Test
  void carEventDetectedIsEventValid() {

    // not possible to get private enum value from sut

  }

}