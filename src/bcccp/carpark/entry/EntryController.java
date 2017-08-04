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
  private IAdhocTicket adhocTicket = null;
  private long entryTime;
  private String seasonTicketId = null;


  public EntryController(Carpark carpark, IGate entryGate, ICarSensor os, ICarSensor is, IEntryUI ui) {

    this.carpark = carpark;

    this.entryGate = entryGate;

    outsideSensor = os;

    insideSensor = is;

    this.ui = ui;

  }


  //adhoc
  @Override
  public void buttonPushed() {

    adhocTicket = carpark.issueAdhocTicket();

    carpark.recordAdhocTicketEntry();


  }


  //seasonal
  @Override
  public void ticketInserted(String barcode) {

    if (carpark.isSeasonTicketValid(barcode)){

      carpark.recordSeasonTicketEntry(barcode);


    }



  }


  @Override
  public void ticketTaken() {

    entryGate.raise();


  }

// The number of vehicles in the car park is incremented by 1 and a check is
// made against the capacity of the car park.
  @Override
  public void notifyCarparkEvent() {

    

  }

/*
A ‘Take
Ticket’ display is flashed on the control pillar.  If the car park is full, no ticket is
issued, and a ‘Full’ display is flashed on the control pillar.
 */

/*

When a car approaches an entry barrier, its presence is detected by a sensor
under the road surface, and a ‘Press Button’ display is flashed on the control
pillar.
 */

  @Override
  public void carEventDetected(String detectorId, boolean detected) {



    if (detectorId.equals(outsideSensor.getId())) {

      if(detected == true){

        if (!carpark.isFull()) {

          ui.display("Press Button");


        }else {

          ui.display("Full");
        }


      }

    }


    if (detectorId.equals(insideSensor.getId())) {

      if (detected == true) {

        if (entryGate.isRaised()){

          entryGate.lower();

        }


      }

    }


}

}
