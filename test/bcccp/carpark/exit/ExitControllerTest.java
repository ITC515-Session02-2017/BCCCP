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

class ExitControllerTest {

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

    // Should initialise STATE but enum is private in sut

    // Following code sets up a formatted barcode for adhoc ticket (Type 'A')

    int ticketNum = 203;

    String entryDate = "";

    DateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");

    entryDate = formatter.format(new Date().getTime()); // the string that is encoded (to a bar code)

    String prefix = "0041"; // hex representation of "A". Unicode: U+0041

    String hexNum = Integer.toHexString(ticketNum);

    String hexDate = Long.toHexString(Long.parseLong(entryDate));

    // insert delimiter ":" between hex values
    barcodeForAdhocTicket = new StringBuilder(prefix + ":").append(hexNum + ":").append(hexDate).toString();


    // Following code sets up a formatted barcode for season ticket (Type 'S')

    ticketNum = 465;

    formatter = new SimpleDateFormat("ddMMyyyyhhmmss");

    entryDate = formatter.format(new Date().getTime()); // the string that is encoded (to a bar code)

    prefix = "0053"; // hex representation of "S". Unicode: U+0053

    hexNum = Integer.toHexString(ticketNum);

    hexDate = Long.toHexString(Long.parseLong(entryDate));

    // insert delimiter ":" between hex values
    barcodeForSeasonTicket = new StringBuilder(prefix + ":").append(hexNum + ":").append(hexDate).toString();

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

      fail("Expected: Should throw runtime exception for null as entry gate arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, entry gate arg.", e.getMessage());

    }

  }

  @Test
  void checkConstructorParamOutSensorForNulls() {

    try {

      sut = new ExitController(carpark, exitGate, insideSensor, null, exitUserInterface);

      fail("Expected: Should throw runtime exception for null as entry gate arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, entry gate arg.", e.getMessage());

    }

  }

  @Test
  void checkConstructorParamOutExitUIForNulls() {

    try {

      sut = new ExitController(carpark, exitGate, insideSensor, outsideSensor, null);
      fail("Expected: Should throw runtime exception for null as entry gate arg");

    } catch (RuntimeException e) {

      assertEquals("Invalid argument passed to Carpark constructor, entry gate arg.", e.getMessage());

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

    // not possible to get private enum value from sut

  }

  @Test
  void ticketInsertedCheckProcessingValidAdhocTicket() {

    sut.ticketInserted(barcodeForAdhocTicket);

    // not currently possible to get private enum value from sut, but need to validate state = PROCESSED

  }

  @Test
  void ticketInsertedCheckProcessingValidSeasonTicket() {

    sut.ticketInserted(barcodeForSeasonTicket);

    // not currently possible to get private enum value from sut, but need to validate state = PROCESSED

  }

  @Test
  void ticketTaken() {

    // not possible to get private enum value from sut

  }

  @Test
  void carEventDetected() {

    // not possible to get private enum value from sut

  }

}