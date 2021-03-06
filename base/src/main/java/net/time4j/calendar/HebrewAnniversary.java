/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2017 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (HebrewAnniversary.java) is part of project Time4J.
 *
 * Time4J is free software: You can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Time4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Time4J. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------
 */

package net.time4j.calendar;

import net.time4j.PlainDate;
import net.time4j.engine.CalendarDate;
import net.time4j.engine.CalendarDays;
import net.time4j.engine.ChronoFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.time4j.calendar.HebrewMonth.HESHVAN;
import static net.time4j.calendar.HebrewMonth.KISLEV;
import static net.time4j.calendar.HebrewMonth.TEVET;
import static net.time4j.calendar.HebrewMonth.SHEVAT;
import static net.time4j.calendar.HebrewMonth.ADAR_I;
import static net.time4j.calendar.HebrewMonth.ADAR_II;


/**
 * <p>The Hebrew calendar has at least two important personal days which can be determined
 * by the methods of this class. </p>
 *
 * @author  Meno Hochschild
 * @since   3.37/4.32
 */
/*[deutsch]
 * <p>Der hebr&auml;ische Kalender hat wenigstens zwei Personentage, deren j&auml;hrliches Datum
 * hier bestimmt werden kann. </p>
 *
 * @author  Meno Hochschild
 * @since   3.37/4.32
 */
public enum HebrewAnniversary {

    //~ Statische Felder/Initialisierungen --------------------------------

    /**
     * Marks the annual birthday in hebrew calendar.
     *
     * <p>The underlying rule takes care of possible leap days (relevant for HESHVAN and KISLEV) and leap months.
     * Example for determining the 13th birthday of a boy born in AM-5776-ADAR_I-30 (<em>bar mitzvah</em>): </p>
     *
     * <pre>
     *     HebrewCalendar birth = HebrewCalendar.of(5776, HebrewMonth.ADAR_I, 30);
     *     assertThat(
     *          birth.get(HebrewAnniversary.BIRTHDAY.inHebrewYear(birth.getYear() + 13)),
     *          is(HebrewCalendar.of(5789, HebrewMonth.NISAN, 1)));
     * </pre>
     *
     * <p>Note: The predefined method {@link HebrewCalendar#barMitzvah()} is simpler to use
     * for this special purpose. </p>
     */
    /*[deutsch]
     * Markiert den j&auml;hrlichen Geburtstag.
     *
     * <p>Die zugrundeliegende Regel ber&uuml;cksichtigt m&ouml;gliche Schalttage (relevant
     * f&uuml;r HESHVAN und KISLEV) und Schaltmonate. Beispiel zur Bestimmung des 13. Geburtstags eines
     * Jungen, der zum Datum AM-5776-ADAR_I-30 geboren wurde (<em>bar mitzvah</em>): </p>
     *
     * <pre>
     *     HebrewCalendar birth = HebrewCalendar.of(5776, HebrewMonth.ADAR_I, 30);
     *     assertThat(
     *          birth.get(HebrewAnniversary.BIRTHDAY.inHebrewYear(birth.getYear() + 13)),
     *          is(HebrewCalendar.of(5789, HebrewMonth.NISAN, 1)));
     * </pre>
     *
     * <p>Hinweis: Die vordefinierte Methode {@link HebrewCalendar#barMitzvah()} ist f&uuml;r diesen
     * speziellen Zweck einfacher zu nutzen. </p>
     */
    BIRTHDAY() {
        @Override
        public ChronoFunction<CalendarDate, HebrewCalendar> inHebrewYear(int hyear) {
            return (birthDate) -> {
                HebrewCalendar hc = convert(birthDate);
                int hdom = hc.getDayOfMonth();
                if (hc.getMonth() == ADAR_II) {
                    return HebrewCalendar.ofBiblical(
                        hyear,
                        HebrewCalendar.isLeapYear(hyear) ? 13 : 12,
                        hdom);
                } else {
                    HebrewMonth hmonth = hc.getMonth();
                    if ((hmonth == ADAR_I) && !HebrewCalendar.isLeapYear(hyear)) {
                        hmonth = ADAR_II;
                    }
                    if (hdom <= 29) {
                        return HebrewCalendar.of(hyear, hmonth, hdom);
                    } else {
                        return HebrewCalendar.of(hyear, hmonth, 1).plus(CalendarDays.of(hdom - 1));
                    }
                }
            };
        }
    },

    /**
     * Marks the annual death day of a near relative in hebrew calendar.
     *
     * <p>The rules follows the book &quot;Calendrical Calculations&quot; by Dershowitz/Reingold. Some
     * Jewish communities might deviate from these rules, however. </p>
     */
    /*[deutsch]
     * Markiert den j&auml;hrlichen Todestag von nahestehenden Personen im hebr&auml;ischen Kalender.
     *
     * <p>Die Regeln folgen dem Buch &quot;Calendrical Calculations&quot; von Dershowitz/Reingold.
     * Jedoch weichen einige j&uuml;dische Gemeinden eventuell davon ab.. </p>
     */
    YAHRZEIT() {
        @Override
        public ChronoFunction<CalendarDate, HebrewCalendar> inHebrewYear(int hyear) {
            return (deathDate) -> {
                HebrewCalendar hc = convert(deathDate);
                int y = hc.getYear();
                HebrewMonth m = hc.getMonth();
                int d = hc.getDayOfMonth();
                if ((m == HESHVAN) && (d == 30) && (HebrewCalendar.lengthOfMonth(y + 1, HESHVAN) == 29)) {
                    return HebrewCalendar.of(hyear, KISLEV, 1).minus(CalendarDays.ONE);
                } else if ((m == KISLEV) && (d == 30) && (HebrewCalendar.lengthOfMonth(y + 1, KISLEV) == 29)) {
                    return HebrewCalendar.of(hyear, TEVET, 1).minus(CalendarDays.ONE);
                } else if ((m == ADAR_II) && HebrewCalendar.isLeapYear(y)) {
                    return HebrewCalendar.of(hyear, ADAR_II, d);
                } else if ((m.getBiblicalValue(false) == 12) && (d == 30) && !HebrewCalendar.isLeapYear(hyear)) {
                    return HebrewCalendar.of(hyear, SHEVAT, 30);
                } else {
                    return HebrewCalendar.ofBiblical(hyear, m.getBiblicalValue(false), 1).plus(CalendarDays.of(d - 1));
                }
            };
        }
    };

    //~ Methoden ----------------------------------------------------------

    /**
     * <p>Determines the input calendar date as original event date and determines the anniversary day of this event
     * in given hebrew year. </p>
     *
     * @param   hyear   hebrew year
     * @return  chronological function which determines the anniversary for any calendar date
     */
    /*[deutsch]
     * <p>Legt das Eingabedatum als urspr&uuml;ngliches Ereignisdatum fest und bestimmt dazu
     * den hebr&auml;ischen Jahrestag im angegebenen hebr&auml;ischen Jahr. </p>
     *
     * @param   hyear   hebrew year
     * @return  chronological function which determines the anniversary for any calendar date
     */
    public ChronoFunction<CalendarDate, HebrewCalendar> inHebrewYear(int hyear) {
        throw new AbstractMethodError();
    }

    /**
     * <p>Determines the input calendar date as original event date and determines the anniversary days of this event
     * in given gregorian year. </p>
     *
     * @param   gyear   gregorian year
     * @return  chronological function which determines the temporally sorted anniversaries for any calendar date
     */
    /*[deutsch]
     * <p>Legt das Eingabedatum als urspr&uuml;ngliches Ereignisdatum fest und bestimmt dazu die gregorianischen
     * Jahrestage im angegebenen gregorianischen Jahr. </p>
     *
     * @param   gyear   gregorian year
     * @return  chronological function which determines the temporally sorted anniversaries for any calendar date
     */
    public ChronoFunction<CalendarDate, List<PlainDate>> inGregorianYear(int gyear) {

        return (date) -> {
            HebrewCalendar event = convert(date);
            int y = PlainDate.of(gyear, 1, 1).transform(HebrewCalendar.axis()).getYear();
            PlainDate d1 = this.inHebrewYear(y).apply(event).transform(PlainDate.axis());
            PlainDate d2 = this.inHebrewYear(y + 1).apply(event).transform(PlainDate.axis());
            List<PlainDate> result = new ArrayList<>(2);
            if (d1.getYear() == gyear) {
                result.add(d1);
            }
            if (d2.getYear() == gyear) {
                result.add(d2);
            }
            return Collections.unmodifiableList(result);
        };

    }

    private static HebrewCalendar convert(CalendarDate date) {

        return (date instanceof HebrewCalendar) ? ((HebrewCalendar) date) : date.transform(HebrewCalendar.axis());

    }

}
