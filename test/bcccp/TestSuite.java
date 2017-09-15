package bcccp;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import bcccp.tickets.adhoc.AdhocTicketTest;
import bcccp.tickets.season.SeasonTicketTest;
import bcccp.carpark.CarparkTest;
import bcccp.carpark.entry.EntryControllerTest;
import bcccp.carpark.exit.ExitControllerTest;


@RunWith(Suite.class)

@Suite.SuiteClasses({
        AdhocTicketTest.class,
        SeasonTicketTest.class,
        CarparkTest.class,
        EntryControllerTest.class,
        ExitControllerTest.class
})


/**
 *
 * The above 'way' works in earlier versions of junit but not junit 5.
 *
 * Aggregating multiple test classes in a test suite.
 *
 * JUnit 5 provides two annotations: @SelectPackages and @SelectClasses to create test suites:
 * https://howtodoinjava.com/junit-5/junit5-test-suites-examples/
 *
 * These annotations are not presently supported in Intellij.
 *
 * "Keep in mind that at this early stage most IDEs do not support those features." http://www.baeldung.com/junit-5
 *
 */
public class TestSuite {
}
