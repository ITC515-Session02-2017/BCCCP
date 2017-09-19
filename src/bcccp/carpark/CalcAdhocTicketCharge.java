package bcccp.carpark;

import java.math.BigDecimal;

import java.util.Calendar;

public abstract class CalcAdhocTicketCharge {

  static final BigDecimal OUT_OF_HOURS_RATE = new BigDecimal(2.0);

  static final BigDecimal BUSINESS_HOURS_RATE = new BigDecimal(5.0);

  static final BigDecimal START_BUS_HOURS = new BigDecimal(7.0); // uses 24-hour clock

  static final BigDecimal END_BUS_HOURS = new BigDecimal(19.0); // uses 24-hour clock

  static final BigDecimal HOURS_CONVERTER = new BigDecimal(1000 * 60 * 60); // used to convert milliseconds to hours

  // These constants are used in place of enum due to the need to compare against int values.
  static final int SUNDAY = 0;
  static final int MONDAY = 1;
  static final int TUESDAY = 2;
  static final int WEDNESDAY = 3;
  static final int THURSDAY = 4;
  static final int FRIDAY = 5;
  static final int SATURDAY = 6;



  static BigDecimal totalCharge = new BigDecimal(0.0);

  static Calendar currentDateTime = Calendar.getInstance();

  static Calendar startDateTime = Calendar.getInstance();

  /**
   * Description: This method returns the charge calculated for a car's total stay
   * in the carpark, using the entry date and time and the current date and time.
   *
   * @param entryDateTime time and date the car entered the carpark
   *                      (in milliseconds since 1 Jan 1970)
   * @return returns the charge as float value
   */
  public static float calculateAddHocTicketCharge (long entryDateTime) {

    long endTime = currentDateTime.getTimeInMillis();

    totalCharge = calcCharge(entryDateTime, endTime);

    return totalCharge.floatValue();
  }

  /**
   * Description: This method calculates the total charge for the car's stay in the carpark
   * which may be over several days. It calls the calcDayCharge method to calculate the charge
   * for each individual day that is included in the total duration of stay.
   *
   * @param sTime start time of car's total stay in carpark in milliseconds
   *              (since since 1 Jan 1970)
   * @param eTime end time of car's total stay in carpark in milliseconds
   *              (since since 1 Jan 1970)
   * @return returns the charge calculated from the duration of stay in currency $.cc
   */
  public static BigDecimal calcCharge (long sTime, long eTime) {

    BigDecimal charge = new BigDecimal(0.0);

    startDateTime.setTimeInMillis(sTime);

    currentDateTime.setTimeInMillis(eTime);

    int startDay = startDateTime.get(Calendar.DAY_OF_YEAR);

    int startDayOfWeek;

    long startTime = startDateTime.getTimeInMillis();

    int startYear = startDateTime.get(Calendar.YEAR);

    int endDay = currentDateTime.get(Calendar.DAY_OF_YEAR);

    int endDayOfWeek = currentDateTime.get(Calendar.DAY_OF_WEEK);

    long endTime = currentDateTime.getTimeInMillis();

    int endYear = currentDateTime.get(Calendar.YEAR);

    long midnight;

    int maxDaysInStartYear = startDateTime.getActualMaximum(Calendar.DAY_OF_YEAR);

    int daysBetweenStartDayEndDay = 0;

    if (startYear < endYear) {

      daysBetweenStartDayEndDay = (maxDaysInStartYear - startDay) + endDay;

    } else {

      daysBetweenStartDayEndDay = endDay - startDay;

    }

    // The while loop calculates the charge for each day prior to the end day by
    // calling method calcDayCharge. It increments the day of the year until it reaches
    // the end day.

    while (daysBetweenStartDayEndDay > 0) {

      // to find midnight of the starting day, get values for year, month, day
      // and then set the time in milliseconds to year, month, day and 59 minutes, 59 seconds.

      startDay = startDateTime.get(Calendar.DAY_OF_YEAR);

      startDayOfWeek = startDateTime.get(Calendar.DAY_OF_WEEK);

      startTime = startDateTime.getTimeInMillis();

      // Set the end time of the current day to midnight (23:59:59)

      startDateTime.set(Calendar.HOUR_OF_DAY, 23);

      startDateTime.set(Calendar.MINUTE, 59);

      startDateTime.set(Calendar.SECOND, 59);

      midnight = startDateTime.getTimeInMillis();

      charge = charge.add(calcDayCharge(startTime, midnight, startDayOfWeek));

      startDateTime.add(Calendar.DAY_OF_YEAR, 1); // increment to next day

      daysBetweenStartDayEndDay--;

      // Set the start time of next day to 00:00:00 hours, minutes, seconds

      startDateTime.set(Calendar.HOUR_OF_DAY, 0);

      startDateTime.set(Calendar.MINUTE, 0);

      startDateTime.set(Calendar.SECOND, 0);

    }

    // last day is calculated from start of the day until the end time.

    charge = charge.add(calcDayCharge(startTime, endTime, endDayOfWeek));

    return charge;
  }

  /**
   * Description: This method calculates the charge for a single day of a car's stay. It is
   * called by calcCharge method for each day included in the car's total stay.
   *
   * @param sTime start time of this day of car's stay in carpark in milliseconds
   * @param eTime end time of this day of car's stay in carpark in milliseconds
   * @param dayOfWeek number of this day of the week (0 = Sunday, etc.)
   * @return returns the charge calculated for this day in currency $.cc (BigDecimal)
   */
  public static BigDecimal calcDayCharge (long sTime, long eTime, int dayOfWeek) {

    BigDecimal decimalSTime = new BigDecimal(sTime);

    BigDecimal decimalETime = new BigDecimal(eTime);

    BigDecimal dayCharge = new BigDecimal(0.0);

    // Put start of business hours into milliseconds format

    Calendar startBusHours = Calendar.getInstance();

    startBusHours.setTimeInMillis(sTime);

    startBusHours.set(Calendar.HOUR_OF_DAY, START_BUS_HOURS.intValue());

    long sBusHours = startBusHours.getTimeInMillis();

    BigDecimal decimalSBusHours = new BigDecimal(sBusHours);

    // Put end of business hours into milliseconds format

    Calendar endBusHours = Calendar.getInstance();

    endBusHours.setTimeInMillis(eTime);

    endBusHours.set(Calendar.HOUR_OF_DAY, END_BUS_HOURS.intValue());

    long eBusHours = endBusHours.getTimeInMillis();

    BigDecimal decimalEBusHours = new BigDecimal(eBusHours);

    boolean isBusinessDay = true;

    if (dayOfWeek == 1 || dayOfWeek == 2 || dayOfWeek == 3 || dayOfWeek == 4 || dayOfWeek == 5) {

      isBusinessDay = true;

    } else {

      isBusinessDay = false;

    }

    if (isBusinessDay) {

      if (sTime <= sBusHours) {

        dayCharge = (decimalETime.subtract(decimalSTime)).multiply(OUT_OF_HOURS_RATE);

      } else if (sTime >= eBusHours) {

        dayCharge = (decimalETime.subtract(decimalSTime)).multiply(OUT_OF_HOURS_RATE);

      } else if (sTime >= sBusHours && eTime <= eBusHours) {

        dayCharge = (decimalETime.subtract(decimalSTime)).multiply(BUSINESS_HOURS_RATE);

      } else if (sTime < sBusHours && eTime <= eBusHours) {

        dayCharge = (decimalSBusHours.subtract(decimalSTime)).multiply(OUT_OF_HOURS_RATE);

        dayCharge = ((dayCharge.add(decimalETime)).subtract(decimalEBusHours)).multiply(BUSINESS_HOURS_RATE);

      } else if (sTime >= sBusHours && sTime < eBusHours &&
              eTime > eBusHours) {

        dayCharge = (decimalEBusHours.subtract(decimalSTime)).multiply(BUSINESS_HOURS_RATE);

        dayCharge = ((dayCharge.add(decimalETime)).subtract(decimalEBusHours)).multiply(OUT_OF_HOURS_RATE);

      } else if (sTime < sBusHours && eTime > eBusHours) {

        dayCharge = (decimalSBusHours.subtract(decimalSTime)).multiply(OUT_OF_HOURS_RATE);

        dayCharge = ((dayCharge.add(decimalEBusHours).subtract(decimalSBusHours))).multiply(BUSINESS_HOURS_RATE);

        dayCharge = ((dayCharge.add(decimalETime).subtract(decimalEBusHours))).multiply(OUT_OF_HOURS_RATE);

      } else {

        throw new RuntimeException("CalcAdhocTicketCharge error: start and end hours of day are invalid");

      }

    } else { // not a business day

      dayCharge = (decimalETime.subtract(decimalSTime)).multiply(OUT_OF_HOURS_RATE);

      }

    return dayCharge.divide(HOURS_CONVERTER);
  }

}
