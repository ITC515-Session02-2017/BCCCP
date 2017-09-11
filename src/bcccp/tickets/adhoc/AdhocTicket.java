package bcccp.tickets.adhoc;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;

/**
 * A ticket for casual carpark users.
 */
public class AdhocTicket implements IAdhocTicket {


    private String carparkId;
    private int ticketNo;
    private long entryDateTime;
    private long paidDateTime;
    private long exitDateTime;
    private float charge;
    private String barcode;
    private STATE state;

    private enum STATE {
        ISSUED,
        CURRENT,
        PAID,
        EXITED
    }

    /**
     * A ticket for casual carpark clients.
     *
     * @param carparkId the carpark. Cannot be null or empty.
     * @param ticketNo  the ticket number. Cannot be zero or negative.
     * @param barcode   the string of values encoded by the barcode. Cannot be null or empty.
     */
    public AdhocTicket(String carparkId, int ticketNo, String barcode) {

        if (isValue(carparkId) && isValidID(ticketNo) && isValue(barcode)) {

            this.carparkId = carparkId;

            this.ticketNo = ticketNo;

            this.barcode = barcode;

            barCodeInfo(barcode);

            state = STATE.ISSUED;

        } else {

            throw new IllegalArgumentException(
                    "Invalid Input: check that the arguments passed to the constructor " + "are valid.");
        }
    }

    @Override
    public int getTicketNo() {

        return ticketNo;
    }

    @Override
    public String getBarcode() {
        return barcode;
    }

    @Override
    public String getCarparkId() {
        return carparkId;
    }

    @Override
    public void enter(long dateTime) {

        entryDateTime = dateTime;

        state = STATE.CURRENT;
    }

    @Override
    public long getEntryDateTime() {

        return entryDateTime;
    }

    @Override
    public boolean isCurrent() {

        return state == STATE.CURRENT;
    }

    @Override
    public void pay(long dateTime, float charge) {

        paidDateTime = dateTime;

        this.charge = charge;

        state = STATE.PAID;
    }

    @Override
    public long getPaidDateTime() {

        return paidDateTime;
    }

    @Override
    public boolean isPaid() {

        return state == STATE.PAID;
    }

    @Override
    public float getCharge() {

        return charge;
    }

    public String toString() {
        Date entryDate = new Date(entryDateTime);
        Date paidDate = new Date(paidDateTime);
        Date exitDate = new Date(exitDateTime);

        return "Carpark: " + carparkId + "\n"
                + "Ticket No: " + ticketNo + "\n"
                + "Entry Time: " + entryDate + "\n"
                + "Paid Time: " + paidDate + "\n"
                + "Exit Time: " + exitDate + "\n"
                + "State: " + state + "\n"
                + "Barcode: " + barcode;
    }

    @Override
    public void exit(long dateTime) {

        exitDateTime = dateTime;

        state = STATE.EXITED;
    }

    @Override
    public long getExitDateTime() {

        return exitDateTime;
    }

    @Override
    public boolean hasExited() {

        return state == STATE.EXITED;
    }

    private Boolean isValue(String str) {

        return (str != null && !str.isEmpty());
    }

    private Boolean isValidID(int id) {

        return id > 0;
    }

    /**
     * Description: - Method for conversion of hex encoded ticket details
     *
     * @param barcode
     */
    private void barCodeInfo(String barcode) {

        // U+002D ‭ - unicode HYPHEN-MINUS
        Pattern p = Pattern.compile("\u002D", Pattern.LITERAL);

        String[] elements = p.split(barcode);

        System.out.println("barcode:" + barcode);

        if (elements[1].length() % 2 == 1) elements[1] = "0" + elements[1];

        if (elements[2].length() % 2 == 1) elements[2] = "0" + elements[2];

        System.out.println("Datehex:" + elements[2]);

        ticketNo = Integer.parseInt(elements[1], 16);

        System.out.println("Ticket Number: " + ticketNo);

        byte[] entryDateBytes = DatatypeConverter.parseHexBinary(elements[2]);

        String entryDate = null;
        try {
            entryDate = new String(entryDateBytes, "UTF-16");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println("DateString:" + entryDate);

        entryDateTime =
                new Date(
                        Integer.parseInt(entryDate.substring(4, 8)) - 1900,
                        Integer.parseInt(entryDate.substring(2, 4)) - 1,
                        Integer.parseInt(entryDate.substring(0, 2)),
                        Integer.parseInt(entryDate.substring(8, 10)),
                        Integer.parseInt(entryDate.substring(10, 12)),
                        Integer.parseInt(entryDate.substring(12)))
                        .getTime();
    }

}