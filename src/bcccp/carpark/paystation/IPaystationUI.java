package bcccp.carpark.paystation;

public interface IPaystationUI {
    void registerController(IPaystationController controller);

    void deregisterController();

    void printTicket(String carparkId, int ticketNo, long entryTime, long paidTime, float charge, String barcode);

    void display(String message);

    void beep();

}
