package bcccp.tickets.adhoc;

import java.util.Date;
import javax.xml.bind.DatatypeConverter;

/**
 * A ticket for casual carpark users.
 */
public final class AdhocTicket implements IAdhocTicket {

  private String carparkId;
  private int ticketNo;
  private long entryDateTime;
  private long paidDateTime;
  private long exitDateTime;
  private float charge;
  private String barcode;
  private STATE state;

  private enum STATE {ISSUED, CURRENT, PAID, EXITED}

  /**
   * A ticket for casual carpark clients.
   *
   * @param carparkId the carpark. Cannot be null or empty.
   * @param ticketNo the ticket number. Cannot be zero or negative.
   * @param barcode the string of values encoded by the barcode. Cannot be null or empty.
   */
  public AdhocTicket(String carparkId, int ticketNo, String barcode) {

    if (isValue(carparkId) && isValue(barcode) && isValidID(ticketNo)) {

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
   * Description:
   * - Method for conversion of hex endcoded ticket details
   *
   * @param barcode
   */
  private void barCodeInfo(String barcode) {

    String[] elements = barcode.split(":");

    byte[] ticketBytes = DatatypeConverter.parseHexBinary(elements[1]);

    byte[] entryDateBytes = DatatypeConverter.parseHexBinary(elements[2]);

    ticketNo = Integer.parseInt(new String(ticketBytes));

    String tmp = new String(entryDateBytes);
    // the constructor for Date used below is deprecated but serves the purpose.
    // Refer: http://docs.oracle.com/javase/6/docs/api/java/util/Date.html for explanation of "- 1900", etc.
    entryDateTime =
            new Date(
                    Integer.parseInt(tmp.substring(4, 8)) - 1900,
                    Integer.parseInt(tmp.substring(2, 4)) - 1,
                    Integer.parseInt(tmp.substring(0, 2)),
                    Integer.parseInt(tmp.substring(8, 10)),
                    Integer.parseInt(tmp.substring(10, 12)),
                    Integer.parseInt(tmp.substring(12)))
                    .getTime();

  }


}
