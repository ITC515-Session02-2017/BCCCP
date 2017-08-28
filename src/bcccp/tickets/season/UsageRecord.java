package bcccp.tickets.season;

public class UsageRecord implements IUsageRecord {

  String ticketId;
  long startDateTime;
  long endDateTime;

  /**
   * This class represents each Season Ticket object, which is instantiated in Main method
   *
   * @param ticketId unique identifier for each Season Ticket
   * @param startDateTime start date of season ticket usage on a date
   */
  public UsageRecord(String ticketId, long startDateTime) {
    this.ticketId = ticketId;
    this.startDateTime = startDateTime;
  }

  @Override
  public void finalise(long endDateTime) {
    this.endDateTime = endDateTime;
  }

  @Override
  public long getStartTime() {
    return startDateTime;
  }

  @Override
  public long getEndTime() {
    return endDateTime;
  }

  @Override
  public String getSeasonTicketId() {
    return ticketId;
  }
}
