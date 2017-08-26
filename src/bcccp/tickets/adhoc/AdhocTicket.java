package bcccp.tickets.adhoc;

import java.util.Date;

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

  /**
   * A ticket for casual carpark clients.
   *
   * @param carparkId the carpark
   * @param ticketNo  the ticket number
   * @param barcode   the string of values encoded by the barcode
   */
  public AdhocTicket(String carparkId, int ticketNo, String barcode) {

    this.carparkId = carparkId;
    
    this.ticketNo = ticketNo;

    this.barcode = barcode;
    
    entryDateTime =
            new Date(
                    Integer.parseInt(barcode.substring(4, 8)) - 1900,
                    Integer.parseInt(barcode.substring(2, 4)) - 1,
                    Integer.parseInt(barcode.substring(0, 2)),
                    Integer.parseInt(barcode.substring(8, 10)),
                    Integer.parseInt(barcode.substring(10, 12)),
                    Integer.parseInt(barcode.substring(12)))
                    .getTime();
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
  }

  @Override
  public long getEntryDateTime() {

    return entryDateTime;
  }

  @Override
  public boolean isCurrent() {

    return entryDateTime > 0 && paidDateTime == 0;
  }

  @Override
  public void pay(long dateTime, float charge) {

    paidDateTime = dateTime;

    this.charge = charge;
  }

  @Override
  public long getPaidDateTime() {

    return paidDateTime;
  }

  @Override
  public boolean isPaid() {

    return paidDateTime > 0;
  }

  @Override
  public float getCharge() {

    return charge;
  }

  @Override
  public void exit(long dateTime) {

    exitDateTime = dateTime;
  }

  @Override
  public long getExitDateTime() {

    return exitDateTime;
  }

  @Override
  public boolean hasExited() {

    return exitDateTime > 0;
  }
}