package bcccp.carpark.entry;

public interface IEntryUI {
    void registerController(IEntryController controller);

    void deregisterController();

    void display(String message);

    void printTicket(String id);

    boolean ticketPrinted();

    void discardTicket();

    void beep();

}