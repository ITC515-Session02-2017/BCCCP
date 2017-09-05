package bcccp.carpark;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bcccp.tickets.adhoc.IAdhocTicket;
import bcccp.tickets.adhoc.IAdhocTicketDAO;
import bcccp.tickets.season.ISeasonTicket;
import bcccp.tickets.season.ISeasonTicketDAO;

public class Carpark implements ICarpark {

    private List<ICarparkObserver> observers;
    private String carparkId;
    private int capacity;
    private int numberOfCarsParked;
    private IAdhocTicketDAO adhocTicketDAO;
    private ISeasonTicketDAO seasonTicketDAO;

    /**
     * This class represents the car park, registers entry and exit of cars and registers tickets,
     * both ad hoc and season
     *
     * @param name            short or long term car park
     * @param capacity        total number of cars that can park in it
     * @param adhocTicketDAO  record of ad hoc ticket
     * @param seasonTicketDAO record of season ticket
     */
    public Carpark(
            String name, int capacity, IAdhocTicketDAO adhocTicketDAO, ISeasonTicketDAO seasonTicketDAO) {

        this.carparkId = name;
        this.capacity = capacity;
        this.adhocTicketDAO = adhocTicketDAO;
        this.seasonTicketDAO = seasonTicketDAO;

        observers = new ArrayList<>();
    }

    @Override
    public void register(ICarparkObserver observer) {

        observers.add(observer);
    }

    @Override
    public void deregister(ICarparkObserver observer) {

        observers.remove(observer);
    }

    @Override
    public String getName() {

        return carparkId;
    }

    @Override
    public boolean isFull() {

        return numberOfCarsParked >= capacity;
    }

    @Override
    public IAdhocTicket issueAdhocTicket() {

        return adhocTicketDAO.createTicket(carparkId);
    }

    @Override
    public void recordAdhocTicketEntry() {

        numberOfCarsParked++;
    }

    @Override
    public IAdhocTicket getAdhocTicket(String barcode) {

        return adhocTicketDAO.findTicketByBarcode(barcode);
    }

    @Override
    public float calculateAddHocTicketCharge(long entryDateTime) {
        // Calculation: get current Date and Time, subtract entryDateTime, multiply result by
        // the $ charge rate and return charge. Assumption that rates are a fixed rate of $5.00 per hour.
        // Convert time to hours by dividing by 60,000. Assumption this is short-stay tariff car park.

        float rates = 5.0f;

        Date dateTime = new Date();

        float chargeAmount = (dateTime.getTime() - entryDateTime) * rates / 60000;

        return chargeAmount;
    }

    @Override
    public void recordAdhocTicketExit() {

        numberOfCarsParked--;
    }

    @Override
    public void registerSeasonTicket(ISeasonTicket seasonTicket) {

        seasonTicketDAO.registerTicket(seasonTicket);
    }

    @Override
    public void deregisterSeasonTicket(ISeasonTicket seasonTicket) {

        seasonTicketDAO.deregisterTicket(seasonTicket);
    }

    @Override
    public boolean isSeasonTicketValid(String ticketId) {

        // If today's date is within the startValidPeriod and endValidPeriod, the season ticket is valid

        Date dateTime = new Date();

        ISeasonTicket sTicket = seasonTicketDAO.findTicketById(ticketId);

        return (dateTime.getTime() >= sTicket.getStartValidPeriod())
                && (dateTime.getTime() <= sTicket.getEndValidPeriod());
    }

    @Override
    public boolean isSeasonTicketInUse(String ticketId) {

        ISeasonTicket sTicket = seasonTicketDAO.findTicketById(ticketId);

        return sTicket.inUse();
    }

    @Override
    public void recordSeasonTicketEntry(String ticketId) {

        seasonTicketDAO.recordTicketEntry(ticketId);
    }

    @Override
    public void recordSeasonTicketExit(String ticketId) {

        seasonTicketDAO.recordTicketExit(ticketId);
    }
}
