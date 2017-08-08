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


  public EntryController(Carpark carpark, IGate entryGate, ICarSensor os, ICarSensor is,
      IEntryUI ui) {

    this.carpark = carpark;

    this.entryGate = entryGate;

    outsideSensor = os;

    insideSensor = is;

    this.ui = ui;

  }


  @Override
  public void buttonPushed() {

    IAdhocTicket adhocTicket = carpark.issueAdhocTicket();

    ui.printTicket(carpark.getName(), adhocTicket.getTicketNo(), adhocTicket.getEntryDateTime(),
        adhocTicket.getBarcode());

    ui.display("Take Ticket");

  }



  @Override
  public void ticketInserted(String barcode) {

    if (carpark.isSeasonTicketValid(barcode)) {

      carpark.recordSeasonTicketEntry(barcode);

      ui.discardTicket();   // eject valid ticket

    } else {

      ui.discardTicket();   // reject invalid ticket

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
