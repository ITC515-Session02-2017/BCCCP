package bcccp.carpark.exit;

import bcccp.carpark.Carpark;
import bcccp.carpark.ICarSensor;
import bcccp.carpark.ICarSensorResponder;
import bcccp.carpark.ICarpark;
import bcccp.carpark.IGate;
import bcccp.tickets.adhoc.IAdhocTicket;
import java.util.Date;

public class ExitController implements ICarSensorResponder, IExitController {

  static final long FIFTEEN_MINUTES = 900000; //fifteen minutes = 900000 milliseconds
  private IGate exitGate;
  private ICarSensor insideSensor;
  private ICarSensor outsideSensor;
  private IExitUI ui;
  private ICarpark carpark;
  private IAdhocTicket adhocTicket = null;
  private long exitTime;
  private String seasonTicketId = null;

    private enum STATE {
        IDLE,
        WAITING,
        PROCESSED,
        REJECTED,
        TAKEN,
        EXITING,
        EXITED,
        BLOCKED
    }

    private STATE state;
    private STATE prevState;
    private String message;

  /**
   * Description - a controller class for sensing the approach and departure of cars at the car park
   * exit gate.
   *
   * @param carpark short or long term carpark
   * @param exitGate exit gate.
   * @param is sensor inside of gate
   * @param os sensor outside of gate
   * @param ui control pillar user interface
   */
  public ExitController(Carpark carpark, IGate exitGate, ICarSensor is, ICarSensor os, IExitUI ui) {

      if (carpark != null && exitGate != null && os != null && is != null && ui != null) {

          this.carpark = carpark;

          this.exitGate = exitGate;

          insideSensor = is;

          outsideSensor = os;

          this.ui = ui;

          os.registerResponder(this);

          is.registerResponder(this);

          ui.registerController(this);

          prevState = STATE.IDLE;

          setState(STATE.IDLE);

      } else {

          throw new RuntimeException("Arguments cannot be null.");
      }
  }

  // STEP: Read barcode.
  // The bar code is read and a check is made that no more than 15 minutes have elapsed.
  @Override
  public void ticketInserted(String ticketStr) {

      if (state == STATE.WAITING) {
          if (isAdhocTicket(ticketStr)) {
              adhocTicket = carpark.getAdhocTicket(ticketStr);
              exitTime = new Date().getTime();
              if (adhocTicket != null && adhocTicket.isPaid()) {
                  if (exitTime < (adhocTicket.getPaidDateTime() + FIFTEEN_MINUTES)) {
                      setState(STATE.PROCESSED);
                  }
              } else {
          ui.beep();
                  setState(STATE.REJECTED);
              }
          } else if (carpark.isSeasonTicketValid(ticketStr) && carpark.isSeasonTicketInUse(ticketStr)) {
              seasonTicketId = ticketStr;
              setState(STATE.PROCESSED);
          } else {
        ui.beep();
              setState(STATE.REJECTED);
          }
      } else {
          ui.beep();
          ui.discardTicket();
          log("ticketInserted: called while in incorrect state");
          setState(STATE.REJECTED);
    }
  }

  @Override
  public void ticketTaken() {

      if (state == STATE.PROCESSED) {

          carpark.getAdhocTicket(adhocTicket.getBarcode()).exit(exitTime);
          exitGate.raise();
          setState(STATE.TAKEN);
      } else if (state == STATE.REJECTED) {
          setState(STATE.WAITING);
      } else {
          ui.beep();
          log("ticketTaken: called while in incorrect state");
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
              if (detectorId.equals(insideSensor.getId()) && detected) {
                  log("eventDetected: setting state to WAITING");
                  setState(STATE.WAITING);
              } else if (detectorId.equals(outsideSensor.getId()) && detected) {
                  setState(STATE.BLOCKED);
              }
              break;

          case WAITING:
          case PROCESSED:
              if (detectorId.equals(insideSensor.getId()) && !detected) {
                  setState(STATE.IDLE);
              } else if (detectorId.equals(outsideSensor.getId()) && detected) {
                  setState(STATE.BLOCKED);
              }
              break;

          case TAKEN:
              if (detectorId.equals(insideSensor.getId()) && !detected) {
                  setState(STATE.IDLE);
              } else if (detectorId.equals(outsideSensor.getId()) && detected) {
                  setState(STATE.EXITING);
              }
              break;

          case EXITING:
              if (detectorId.equals(insideSensor.getId()) && !detected) {
                  setState(STATE.EXITED);
              } else if (detectorId.equals(outsideSensor.getId()) && !detected) {
                  setState(STATE.TAKEN);
              }
              break;

          case EXITED:
              if (detectorId.equals(insideSensor.getId()) && detected) {
                  setState(STATE.EXITING);
              } else if (detectorId.equals(outsideSensor.getId()) && !detected) {
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
                if (prevState == STATE.EXITED) {
                    if (adhocTicket != null) {
                        adhocTicket.exit(exitTime);
                        carpark.recordAdhocTicketExit();
                        log(adhocTicket.toString());
                    } else if (seasonTicketId != null) {
                        carpark.recordSeasonTicketExit(seasonTicketId);
                    }
                }
                adhocTicket = null;
                seasonTicketId = null;

                message = "Idle";
                state = STATE.IDLE;
                //prevMessage = message;
                prevState = state;
                ui.display(message);
                if (insideSensor.carIsDetected()) {
                    setState(STATE.WAITING);
                }
                if (exitGate.isRaised()) {
                    exitGate.lower();
                }
                exitTime = 0;
                break;

            case WAITING:
                log("setState: WAITING");
                message = "Insert Ticket";
                state = STATE.WAITING;
                //prevMessage = message;
                prevState = state;
                ui.display(message);
                if (!insideSensor.carIsDetected()) {
                    setState(STATE.IDLE);
                }
                break;

            case PROCESSED:
                log("setState: PROCESSED");
                message = "Take Processed Ticket";
                state = STATE.PROCESSED;
                prevState = state;
                ui.display(message);
                if (!insideSensor.carIsDetected()) {
                    setState(STATE.IDLE);
                }
                break;

            case REJECTED:
                log("setState: REJECTED");
                message = "Take Rejected Ticket";
                state = STATE.REJECTED;
                prevState = state;
                ui.display(message);
                if (!insideSensor.carIsDetected()) {
                    setState(STATE.IDLE);
                }
                break;

            case TAKEN:
                log("setState: TAKEN");
                message = "Ticket Taken";
                state = STATE.TAKEN;
                prevState = state;
                ui.display(message);
                break;

            case EXITING:
                log("setState: EXITING");
                message = "Exiting";
                state = STATE.EXITING;
                prevState = state;
                ui.display(message);
                break;

            case EXITED:
                log("setState: EXITED");
                message = "Exited";
                state = STATE.EXITED;
                prevState = state;
                ui.display(message);
                break;

            default:
                break;
        }
    }

    private void log(String message) {
        System.out.println("ExitController : " + message);
    }

    private boolean isAdhocTicket(String barcode) {
        return barcode.substring(0, 1).equals("A");
  }

    public STATE getState() {
        return state;
    }

    public STATE getPrevState() {
        return prevState;
    }

}