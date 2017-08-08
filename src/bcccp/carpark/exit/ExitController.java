package bcccp.carpark.exit;

import bcccp.carpark.Carpark;
import bcccp.carpark.ICarSensor;
import bcccp.carpark.ICarSensorResponder;
import bcccp.carpark.ICarpark;
import bcccp.carpark.IGate;
import bcccp.tickets.adhoc.IAdhocTicket;
import java.util.Date;

public class ExitController implements ICarSensorResponder, IExitController {
	
	private IGate exitGate;
	private ICarSensor insideSensor;
	private ICarSensor outsideSensor; 
	private IExitUI ui;
	
	private ICarpark carpark;
	private IAdhocTicket  adhocTicket = null;
	private long exitTime;
	private String seasonTicketId = null;

	private final long FIFTEEN_MINUTES = 900000; //milliseconds


	public ExitController(Carpark carpark, IGate exitGate, ICarSensor is, ICarSensor os, IExitUI ui) {

		this.carpark = carpark;

		this.exitGate = exitGate;

		insideSensor = is;

		outsideSensor = os;

		this.ui = ui;

	}


	// STEP: Read barcode.
// The bar code is read and a check is made that no more than 15 minutes have elapsed.
	@Override
	public void ticketInserted(String ticketStr) {

		exitTime = new Date().getTime();

		adhocTicket = carpark.getAdhocTicket(ticketStr);

		if (adhocTicket != null) {

			if (exitTime < (adhocTicket.getPaidDateTime() + FIFTEEN_MINUTES)) {
				ticketTaken();
			}
			// otherwise:  an intercom in the control pillar is activated and connected to the attendant in the car park office.
			return;

		}

		if (carpark.isSeasonTicketValid(ticketStr)) {
			ticketTaken();
		}

		// otherwise:  an intercom in the control pillar is activated and connected to the attendant in the car park office.

	}

	@Override
	public void ticketTaken() {

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
