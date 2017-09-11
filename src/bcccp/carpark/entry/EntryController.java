package bcccp.carpark.entry;

import bcccp.carpark.Carpark;
import bcccp.carpark.ICarSensor;
import bcccp.carpark.ICarSensorResponder;
import bcccp.carpark.ICarpark;
import bcccp.carpark.ICarparkObserver;
import bcccp.carpark.IGate;
import bcccp.tickets.adhoc.IAdhocTicket;

public class EntryController implements ICarSensorResponder, ICarparkObserver, IEntryController {

  private IAdhocTicket adhocTicket;
  private IGate entryGate;
  private ICarSensor outsideSensor;
  private ICarSensor insideSensor;
  private IEntryUI ui;
  private ICarpark carpark;
  private long entryTime;

  private enum STATE {
    IDLE,
    WAITING,
    FULL,
    VALIDATED,
    ISSUED,
    TAKEN,
    ENTERING,
    ENTERED,
    BLOCKED
  }

  private String seasonTicketId;
  private STATE state;
  private STATE prevState;
  private String message;

  /**
   * Description - a controller for sensing cars approaching and leaving the entry gate, raising and
   * lowering the gate, and communicating information to the 'control pillar' and carpark.
   *
   * @param carpark short term or long term
   * @param entryGate entry gate
   * @param os sensor outside gate.
   * @param is sensor inside gate
   * @param ui control pillar user interface
   */
  public EntryController(Carpark carpark, IGate entryGate, ICarSensor os, ICarSensor is, IEntryUI ui) {

    if (carpark != null && entryGate != null && os != null && is != null && ui != null) {

      this.carpark = carpark;

      this.entryGate = entryGate;

      outsideSensor = os;

      insideSensor = is;

      this.ui = ui;

      outsideSensor.registerResponder(this);

      insideSensor.registerResponder(this);

      ui.registerController(this);

      setState(STATE.IDLE);

    } else {

      throw new RuntimeException("Arguments to constructor cannot be null.");
    }
  }

  @Override
  public void buttonPushed() {
    if (state == STATE.WAITING) {
      if (!carpark.isFull()) {
        adhocTicket = carpark.issueAdhocTicket();

        entryTime = adhocTicket.getEntryDateTime();

        ui.printTicket(adhocTicket.toString());

        setState(STATE.ISSUED);
      } else {
        setState(STATE.FULL);
      }
    } else {
      ui.beep();
      log("ButtonPushed: called while in incorrect state");
    }
  }

  @Override
  public void ticketInserted(String barcode) {

    if (state == STATE.WAITING) {
      try {
        if (carpark.isSeasonTicketValid(barcode) && !carpark.isSeasonTicketInUse(barcode)) {
          seasonTicketId = barcode;
          setState(STATE.VALIDATED);
        } else {
          ui.beep();
          seasonTicketId = null;
          log("ticketInserted: invalid ticket id");
        }
      } catch (NumberFormatException e) {
        ui.beep();
        seasonTicketId = null;
        log("ticketInserted: invalid ticket id");
      }
    } else {
      ui.beep();
      log("ticketInserted: called while in incorrect state");
    }
  }

  @Override
  public void ticketTaken() {

    if (state == STATE.ISSUED || state == STATE.VALIDATED) {

      setState(STATE.TAKEN);

    } else {

      ui.beep();

      log("ticketTaken: called while in incorrect state");
    }
  }

  @Override
  public void notifyCarparkEvent() {

    if (state == STATE.FULL) {

      if (!carpark.isFull()) {

        setState(STATE.WAITING);
      }
    }
  }

  @Override
  public void carEventDetected(String detectorId, boolean detected) {

    log("carEventDetected: " + detectorId + ", car Detected: " + detected);

    switch (state) {
      case BLOCKED:
        if (detectorId.equals(insideSensor.getId()) && !detected) {
          setState(prevState);
        }
        break;

      case IDLE:
        log("eventDetected: IDLE");

        if (detectorId.equals(outsideSensor.getId()) && detected) {
          log("eventDetected: setting state to WAITING");
          setState(STATE.WAITING);
        } else if (detectorId.equals(insideSensor.getId()) && detected) {
          setState(STATE.BLOCKED);
        }
        break;

      case WAITING:
        log("eventDetected: WAITING");

        if (detectorId.equals(outsideSensor.getId()) && detected) {
          log("eventDetected: setting state to WAITING");
          setState(STATE.WAITING);
        } else if (detectorId.equals(insideSensor.getId()) && detected) {
          setState(STATE.BLOCKED);
        }
        break;
      case FULL:
        log("eventDetected: FULL");

        if (detectorId.equals(outsideSensor.getId()) && detected) {
          log("eventDetected: setting state to WAITING");
          setState(STATE.WAITING);
        } else if (detectorId.equals(insideSensor.getId()) && detected) {
          setState(STATE.BLOCKED);
        }
        break;
      case VALIDATED:
      case ISSUED:
        if (detectorId.equals(outsideSensor.getId()) && !detected) {
          setState(STATE.IDLE);
        } else if (detectorId.equals(insideSensor.getId()) && detected) {
          setState(STATE.BLOCKED);
        }
        break;

      case TAKEN:
        if (detectorId.equals(outsideSensor.getId()) && !detected) {
          setState(STATE.IDLE);
        } else if (detectorId.equals(insideSensor.getId()) && detected) {
          setState(STATE.ENTERING);
        }
        break;

      case ENTERING:
        if (detectorId.equals(outsideSensor.getId()) && !detected) {
          setState(STATE.ENTERED);
        } else if (detectorId.equals(insideSensor.getId()) && !detected) {
          setState(STATE.TAKEN);
        }
        break;

      case ENTERED:
        if (detectorId.equals(outsideSensor.getId()) && detected) {
          setState(STATE.ENTERING);
        } else if (detectorId.equals(insideSensor.getId()) && !detected) {
          setState(STATE.IDLE);
        }
        break;

      default:
        break;
    }
  }

  private void setState(STATE newState) {
    switch (newState) {
      case BLOCKED:
        log("setState: BLOCKED");
        prevState = state;
        state = STATE.BLOCKED;
        message = "Blocked";
        ui.display(message);
        break;

      case IDLE:
        log("setState: IDLE");
        if (prevState == STATE.ENTERED) {
          if (adhocTicket != null) {
            adhocTicket.enter(entryTime);
            carpark.recordAdhocTicketEntry();
            entryTime = 0;
            log(adhocTicket.toString());
            adhocTicket = null;
          } else if (seasonTicketId != null) {
            carpark.recordSeasonTicketEntry(seasonTicketId);
            seasonTicketId = null;
          }
        }
        message = "Idle";
        state = STATE.IDLE;
        prevState = state;
        ui.display(message);
        if (outsideSensor.carIsDetected()) {
          setState(STATE.WAITING);
        }
        if (entryGate.isRaised()) {
          entryGate.lower();
        }
        ui.discardTicket();
        break;

      case WAITING:
        log("setState: WAITING");
        message = "Push Button";
        state = STATE.WAITING;
        prevState = state;
        ui.display(message);
        if (!outsideSensor.carIsDetected()) {
          setState(STATE.IDLE);
        }
        break;

      case FULL:
        log("setState: FULL");
        message = "Carpark Full";
        state = STATE.FULL;
        prevState = state;
        if (entryGate.isRaised()) {
          entryGate.lower();
        }
        ui.display(message);
        break;

      case VALIDATED:
        log("setState: VALIDATED");
        message = "Ticket Validated";
        state = STATE.VALIDATED;
        prevState = state;
        ui.display(message);
        if (!outsideSensor.carIsDetected()) {
          setState(STATE.IDLE);
        }
        break;

      case ISSUED:
        log("setState: ISSUED");
        message = "Take Ticket";
        state = STATE.ISSUED;
        prevState = state;
        ui.display(message);
        if (!outsideSensor.carIsDetected()) {
          setState(STATE.IDLE);
        }
        break;

      case TAKEN:
        log("setState: TAKEN");
        message = "Ticket Taken";
        state = STATE.TAKEN;
        prevState = state;
        ui.display(message);
        entryGate.raise();
        break;

      case ENTERING:
        log("setState: ENTERING");
        message = "Entering";
        state = STATE.ENTERING;
        prevState = state;
        ui.display(message);
        notifyCarparkEvent();
        break;

      case ENTERED:
        log("setState: ENTERED");
        message = "Entered";
        state = STATE.ENTERED;
        prevState = state;
        ui.display(message);
        break;

      default:
        break;
    }
  }

  private void log(String message) {
    System.out.println("EntryController : " + message);
  }

  // Following getter method to be removed after testing
  public STATE getState() {
    return state;
   }

  public STATE getPreviousState() {
    return prevState;
   }
}
