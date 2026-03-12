package com.niveshtrack.portfolio.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Date utility methods for portfolio calculations.
 */
public class DateUtils {

    private static final DateTimeFormatter MONTH_LABEL_FORMATTER =
            DateTimeFormatter.ofPattern("MMM yyyy");

    private static final DateTimeFormatter MONTH_KEY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM");

    private DateUtils() {
        // Utility class
    }

    /**
     * Returns the number of calendar days between two dates.
     * Positive if toDate is after fromDate.
     */
    public static long daysBetween(LocalDate fromDate, LocalDate toDate) {
        return ChronoUnit.DAYS.between(fromDate, toDate);
    }

    /**
     * Returns a human-readable month label, e.g. "Jan 2025".
     */
    public static String toMonthLabel(LocalDate date) {
        return date.format(MONTH_LABEL_FORMATTER);
    }

    /**
     * Returns a sortable month key, e.g. "2025-01".
     */
    public static String toMonthKey(LocalDate date) {
        return date.format(MONTH_KEY_FORMATTER);
    }

    /**
     * Determines whether the given date falls within the specified Indian financial year.
     *
     * <p>Indian FY runs from April 1 to March 31.
     * For FY "2024-25": April 1, 2024 → March 31, 2025.
     *
     * @param date           the date to check
     * @param financialYear  e.g. "2024-25"
     * @return true if the date is within the financial year
     */
    public static boolean isInFinancialYear(LocalDate date, String financialYear) {
        int startYear = parseStartYear(financialYear);
        LocalDate fyStart = LocalDate.of(startYear, Month.APRIL, 1);
        LocalDate fyEnd = LocalDate.of(startYear + 1, Month.MARCH, 31);
        return !date.isBefore(fyStart) && !date.isAfter(fyEnd);
    }

    /**
     * Returns the start date (April 1) of a financial year.
     */
    public static LocalDate fyStartDate(String financialYear) {
        return LocalDate.of(parseStartYear(financialYear), Month.APRIL, 1);
    }

    /**
     * Returns the end date (March 31) of a financial year.
     */
    public static LocalDate fyEndDate(String financialYear) {
        return LocalDate.of(parseStartYear(financialYear) + 1, Month.MARCH, 31);
    }

    /**
     * Returns the current Indian financial year string, e.g. "2024-25".
     */
    public static String currentFinancialYear() {
        LocalDate today = LocalDate.now();
        int year = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
        return year + "-" + String.format("%02d", (year + 1) % 100);
    }

    private static int parseStartYear(String financialYear) {
        // Format: "2024-25" → start year 2024
        return Integer.parseInt(financialYear.split("-")[0]);
    }
}
