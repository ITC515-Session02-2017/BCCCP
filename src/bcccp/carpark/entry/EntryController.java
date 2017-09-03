package bcccp.carpark.entry;

import bcccp.carpark.Carpark;
import bcccp.carpark.ICarSensor;
import bcccp.carpark.ICarSensorResponder;
import bcccp.carpark.ICarpark;
import bcccp.carpark.ICarparkObserver;
import bcccp.carpark.IGate;
import bcccp.tickets.adhoc.IAdhocTicket;

public class EntryController implements ICarSensorResponder, ICarparkObserver, IEntryController {

  private IGate entryGate;
  private ICarSensor outsideSensor;
  private ICarSensor insideSensor;
  private IEntryUI ui;
  private ICarpark carpark;

  /**
   * Description - a controller class for sensing cars approaching and leaving the entry gate,
   * raising and lowering the gate, and communicating information to the 'control pillar' and
   * carpark.
   *
   * @param carpark short term or long term
   * @param entryGate entry gate
   * @param os sensor outside gate.
   * @param is sensor inside gate
   * @param ui control pillar user interface
   */
  public EntryController(
      Carpark carpark, IGate entryGate, ICarSensor os, ICarSensor is, IEntryUI ui) {

    if (carpark != null && entryGate != null && os != null && is != null && ui != null) {

      this.carpark = carpark;

      this.entryGate = entryGate;

      outsideSensor = os;

      insideSensor = is;

      this.ui = ui;

    } else {

      throw new RuntimeException("Arguments cannot be null.");
    }
  }

  @Override
  public void buttonPushed() {

    IAdhocTicket adhocTicket = carpark.issueAdhocTicket();

    ui.printTicket(adhocTicket.toString());

    ui.display("Take Ticket");
  }

  @Override
  public void ticketInserted(String barcode) {

    if (carpark.isSeasonTicketValid(barcode)) {

      carpark.recordSeasonTicketEntry(barcode);

      ui.discardTicket(); // eject valid ticket

    } else {

      ui.discardTicket(); // reject invalid ticket
    }
  }

  @Override
  public void ticketTaken() {

    entryGate.raise();
  }

  @Override
  public void notifyCarparkEvent() {

    if (carpark.isFull()) {
      ui.display("Car Park Full");
    }
  }

  @Override
  public void carEventDetected(String detectorId, boolean detected) {

    if (detectorId.equals(outsideSensor.getId())) {

      if (detected) {

        if (!carpark.isFull()) {

          ui.display("Press Button");

        } else {

          ui.display("Full");
        }
      }
    }

    if (detectorId.equals(insideSensor.getId())) {

      if (detected) {

        if (entryGate.isRaised()) {

          entryGate.lower();

          notifyCarparkEvent();
        }
      }
    }
  }
}
