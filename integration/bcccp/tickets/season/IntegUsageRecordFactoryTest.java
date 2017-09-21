package bcccp.tickets.season;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

class IntegUsageRecordFactoryTest {
    static UsageRecordFactory usageRecordFactory;
    static IUsageRecord testRecord;

    Logger logger = Logger.getLogger("Test unit for UsageRecordFactory class");

    @BeforeAll
    static void before() {
        usageRecordFactory = mock(UsageRecordFactory.class);

    }

    @Test
    void testMakeMethod() {
        logger.log(Level.INFO, "Test make method that return an UsageRecord");

        when(usageRecordFactory.make("1234", 4L)).thenReturn(new UsageRecord("1234", 4L));
        usageRecordFactory.make("1234", 4L);
        verify(usageRecordFactory).make("1234", 4L);

    }

    @Test
    void testExceptionWithInvalidParameters() {
        logger.log(Level.INFO,"Test make method with invalid parameters");
        when(usageRecordFactory.make(null, 0)).thenThrow(RuntimeException.class);
    }


}