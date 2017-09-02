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

      } else {

          throw new RuntimeException("Arguments cannot be null.");
      }
  }

  // STEP: Read barcode.
  // The bar code is read and a check is made that no more than 15 minutes have elapsed.
  @Override
  public void ticketInserted(String ticketStr) {

    exitTime = new Date().getTime();

    adhocTicket = carpark.getAdhocTicket(ticketStr);

      if (adhocTicket != null && adhocTicket.isPaid()) {

      if (exitTime < (adhocTicket.getPaidDateTime() + FIFTEEN_MINUTES)) {

          ui.display("Take Processed Ticket");

        ticketTaken();

      } else {
          // otherwise:  an intercom in the control pillar is activated and connected to the attendant
          // in the car park office.
          ui.beep();

          ui.display("Please wait for the parking attendant...");
      }

      return;
    }

    if (carpark.isSeasonTicketValid(ticketStr)) {

        ui.display("Take Processed Ticket");

      ticketTaken();

    } else {
        // otherwise:  an intercom in the control pillar is activated and connected to the attendant
        // in the car park office.
        ui.beep();

        ui.display("Please wait for the parking attendant...");
    }
  }

  @Override
  public void ticketTaken() {

      carpark.getAdhocTicket(adhocTicket.getBarcode()).exit(new Date().getTime());

    exitGate.raise();
  }

  @Override
  public void carEventDetected(String detectorId, boolean detected) {

    if (detectorId.equals(insideSensor.getId())) {

      if (detected) {

        ui.display("Insert Ticket");
      }
    }

    if (detectorId.equals(outsideSensor.getId())) {

      if (detected) {

        if (exitGate.isRaised()) {

          exitGate.lower();
        }
      }
    }
  }
}
