package bcccp.tickets.season;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegrationUsageRecordUsageRecordFactory {

    static UsageRecord usageRecord;
    static UsageRecordFactory usageRecordFactory;
    IUsageRecord iUsageRecord;


    Logger logger = Logger.getLogger("Integration testing for Usage Record and Usage Record Factory");

    @BeforeAll
    static void before(){
        usageRecord = new UsageRecord("123",0L);
        usageRecordFactory = new UsageRecordFactory();
    }

    @Test
    void testContructorsUsageRecord(){
        logger.log(Level.INFO,"Integration test for Usage Record constructor");
        assertEquals("123",usageRecord.getSeasonTicketId());
        assertEquals(0L,usageRecord.getStartTime());
        usageRecord.finalise(1L);
        assertEquals(1L,usageRecord.getEndTime());
    }

    @Test
    void testMakeRecordwithRecordFactory(){
        iUsageRecord = usageRecordFactory.make("123",0L);
        assertEquals("123",iUsageRecord.getSeasonTicketId());
        assertEquals(0L,iUsageRecord.getStartTime());


    }

}
