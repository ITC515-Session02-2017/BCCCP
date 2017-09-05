package bcccp.carpark.paystation;

import bcccp.tickets.adhoc.*;
import bcccp.tickets.season.*;
import bcccp.carpark.Carpark;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

class PaystationControllerTest {

  static String name;
  static int capacity;
  static IAdhocTicketDAO adhocTicketDAO;
  static ISeasonTicketDAO seasonTicketDAO;
  static String barcode;
  static String ticketNumber;
  static String barcodeFormatDateddmmyyyy;
  static String barcodeFormatTimehhmmss;

  static Carpark cp;

  static IPaystationUI userInterface;

  static PaystationController sut;

  static IAdhocTicket ticket;

  @Captor
  private ArgumentCaptor<String> captor;

  @Captor
  private ArgumentCaptor<IPaystationUI> argsList;

  @BeforeAll
  public static void setUp() {

  }

  @BeforeEach
  public void setUpEachTest() {

    name = "Barchester City Carpark";

    capacity = 800;

    // The date and time for the barcode shall be: 3 June 2018 10:26:37
    // Assumption: ticket number is within range of carpark capacity (<=800)

    ticketNumber = String.format("0x02",38);

    barcodeFormatDateddmmyyyy = String.format("0x04",3062018);

    barcodeFormatTimehhmmss = String.format("0x03",102637);

    barcode = "A" + ticketNumber + barcodeFormatDateddmmyyyy + barcodeFormatTimehhmmss;

    MockitoAnnotations.initMocks(this);

    cp = mock(Carpark.class);

    userInterface = mock(IPaystationUI.class);

    sut = new PaystationController(cp, userInterface);

  }

  @AfterEach
  public void clearUp() {

    barcode = "";

    capacity = 0;

    cp = null;

    userInterface = null;

    sut = null;

  }

  @Test
  public void controllerIsStateIdle() throws NullPointerException {

    sut.ticketInserted(barcode);

    verify(userInterface).display(captor.capture());

    assertEquals("Idle", captor.getValue());

  }


  @Test
  public void ticketInsertedIsBarcodeValidAndTicketReturned() throws NullPointerException {

    ticket = cp.getAdhocTicket(barcode);

    assertEquals(barcode, ticket.getBarcode());

  }

  @Test
  public void ticketInsertedAndReturnedCheckIsCurrent() {

    ticket = cp.getAdhocTicket(barcode);

    sut.ticketInserted(ticket.getBarcode());

    when(ticket.isCurrent()).thenReturn(true);

  }

  @Test
  public void ticketInsertedAndReturnedCheckIsNotPaid() throws NullPointerException {

    ticket = cp.getAdhocTicket(barcode);

    sut.ticketInserted(ticket.getBarcode());

    when(ticket.isPaid()).thenReturn(false);

  }

  @Test
  public void ticketInsertedIsChargeCalculated() throws NullPointerException {

    ticket = cp.getAdhocTicket(barcode);

    sut.ticketInserted(ticket.getBarcode());

    verify(userInterface).display(captor.capture());

    assertEquals("AU", captor.getValue().substring(0,1));

  }

  @Test
  public void ticketPaidIsStateWaiting() throws NullPointerException {

    sut.ticketPaid();

    verify(userInterface).display(captor.capture());

    assertEquals("Waiting", captor.getValue());

  }

  @Test
  public void ticketPaidIsTimeAndChargeRecorded() throws NullPointerException {

    ticket = cp.getAdhocTicket(barcode);

    sut.ticketPaid();

    verify(cp).recordAdhocTicketExit();

  }

  @Test
  public void ticketPaidIsPaymentPrinted() throws NullPointerException {

    sut.ticketPaid();

    argsList = ArgumentCaptor.forClass(IPaystationUI.class);

    List<IPaystationUI> numOfArgs = argsList.getAllValues();

    assertEquals(barcode, numOfArgs.get(5));

  }

  @Test
  public void ticketTakenIsStateWaiting() throws NullPointerException {

    sut.ticketTaken();

    verify(userInterface).display(captor.capture());

    assertEquals("Waiting", captor.getValue());
  }

  @Test
  public void ticketTakenIsStatePaid() throws NullPointerException {

    sut.ticketTaken();

    verify(userInterface).display(captor.capture());

    when(captor.capture().equals("Paid")).thenReturn(true);

  }

  @Test
  public void ticketTakenIsStateRejected() throws UnfinishedVerificationException {

    sut.ticketTaken();

    verify(userInterface).display(captor.capture());

    when(captor.capture().equals("Rejected")).thenReturn(true);

  }
}
