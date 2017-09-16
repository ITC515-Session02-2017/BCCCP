package bcccp.carpark;

import java.math.BigDecimal;

import java.util.Calendar;

public abstract class CalcAdhocTicketCharge {

  static final BigDecimal OUT_OF_HOURS_RATE = new BigDecimal(2.0);

  static final BigDecimal BUSINESS_HOURS_RATE = new BigDecimal(5.0);

  static final BigDecimal START_BUS_HOURS = new BigDecimal(7.0); // uses 24-hour clock

  static final BigDecimal END_BUS_HOURS = new BigDecimal(19.0); // uses 24-hour clock

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

    int startHour = startDateTime.get(Calendar.HOUR_OF_DAY);

    int startDayOfWeek = startDateTime.get(Calendar.DAY_OF_WEEK);

    int endDay = currentDateTime.get(Calendar.DAY_OF_YEAR);

    int endHour = currentDateTime.get(Calendar.HOUR_OF_DAY);

    int endDayOfWeek = currentDateTime.get(Calendar.DAY_OF_WEEK);

    final int MIDNIGHT = 00;

    // The while loop calculates the charge for each day prior to the end day by
    // calling method calcDayCharge. It increments the day of the year until it reaches
    // the end day.

    while (startDay != endDay) {

      charge = charge.add(calcDayCharge(startHour, MIDNIGHT, startDayOfWeek));

      startHour = 0;

      startDay++;

      startDateTime.set(Calendar.DAY_OF_YEAR, startDay);

      startDayOfWeek = startDateTime.get(Calendar.DAY_OF_WEEK);

    }

    // last day is calculated from start of the day until the last hour.
    charge = charge.add(calcDayCharge(startHour, endHour, endDayOfWeek));

    return charge;
  }

  /**
   * Description: This method calculates the charge for a single day of a car's stay. It is
   * called by calcCharge method for each day included in the car's total stay.
   *
   * @param sHour start time of this day of car's stay in carpark
   * @param eHour end time of this day of car's stay in carpark
   * @param dayOfWeek number of this day of the week (0 = Sunday, etc.)
   * @return returns the charge calculated for this day
   */
  public static BigDecimal calcDayCharge (int sHour, int eHour, int dayOfWeek) {

    BigDecimal dayCharge = new BigDecimal(0.0);

    BigDecimal decimalSHour = new BigDecimal((sHour));

    BigDecimal decimalEHour = new BigDecimal((eHour));

    boolean isBusinessDay = true;

    if (dayOfWeek == 1 || dayOfWeek == 2 || dayOfWeek == 3 || dayOfWeek == 4 || dayOfWeek == 5) {

      isBusinessDay = true;

    } else {

      isBusinessDay = false;

    }

    if (isBusinessDay) {

      if (eHour <= START_BUS_HOURS.intValue()) {

        dayCharge = (decimalEHour.subtract(decimalSHour)).multiply(OUT_OF_HOURS_RATE);

      } else if (sHour >= END_BUS_HOURS.intValue()) {

        dayCharge = (decimalEHour.subtract(decimalSHour)).multiply(OUT_OF_HOURS_RATE);

      } else if (sHour >= START_BUS_HOURS.intValue() && eHour <= END_BUS_HOURS.intValue()) {

        dayCharge = (decimalEHour.subtract(decimalSHour)).multiply(BUSINESS_HOURS_RATE);

      } else if (sHour < START_BUS_HOURS.intValue() && eHour <= END_BUS_HOURS.intValue()) {

        dayCharge = (START_BUS_HOURS.subtract(decimalSHour)).multiply(OUT_OF_HOURS_RATE);

        dayCharge = (dayCharge.add(decimalEHour.subtract(END_BUS_HOURS))).multiply(BUSINESS_HOURS_RATE);

      } else if (sHour >= START_BUS_HOURS.intValue() && sHour < END_BUS_HOURS.intValue() &&
              eHour > END_BUS_HOURS.intValue()) {

        dayCharge = (END_BUS_HOURS.subtract(decimalSHour)).multiply(BUSINESS_HOURS_RATE);

        dayCharge = (dayCharge.add(decimalEHour.subtract(END_BUS_HOURS))).multiply(OUT_OF_HOURS_RATE);

      } else if (sHour < START_BUS_HOURS.intValue() && eHour > END_BUS_HOURS.intValue()) {

        dayCharge = (START_BUS_HOURS.subtract(decimalSHour)).multiply(OUT_OF_HOURS_RATE);

        dayCharge = (dayCharge.add(END_BUS_HOURS.subtract(START_BUS_HOURS))).multiply(BUSINESS_HOURS_RATE);

        dayCharge = (dayCharge.add(decimalEHour.subtract(END_BUS_HOURS))).multiply(OUT_OF_HOURS_RATE);

      } else {

        throw new RuntimeException("CalcAdhocTicketCharge error: start and end hours of day are invalid");

      }

    } else { // not a business day

      dayCharge = (decimalEHour.subtract(decimalSHour)).multiply(OUT_OF_HOURS_RATE);

      }

    return dayCharge;
  }

}
