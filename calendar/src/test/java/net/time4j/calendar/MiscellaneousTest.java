package net.time4j.calendar;

import net.time4j.PlainDate;
import net.time4j.engine.CalendarDays;
import net.time4j.format.Attributes;
import net.time4j.format.OutputContext;
import net.time4j.format.TextWidth;
import net.time4j.format.expert.ChronoFormatter;
import net.time4j.format.expert.PatternType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(JUnit4.class)
public class MiscellaneousTest {

    @Test
    public void persianCalendarProperties() {
        PersianCalendar date = PersianCalendar.of(1394, PersianMonth.ABAN, 14);
        assertThat(
            date.getDayOfMonth(),
            is(14));
        assertThat(
            date.getMonth(),
            is(PersianMonth.ABAN));
        assertThat(
            date.lengthOfMonth(),
            is(30));
        assertThat(
            date.atTime(12, 0).toDate(),
            is(date));
    }

    @Test
    public void persianCalendarBetween() {
        PersianCalendar start = PersianCalendar.of(1394, PersianMonth.ABAN, 14);
        PersianCalendar end = PersianCalendar.of(1394, PersianMonth.ESFAND, 13);
        assertThat(PersianCalendar.Unit.MONTHS.between(start, end), is(3));
        end = end.plus(CalendarDays.ONE);
        assertThat(PersianCalendar.Unit.MONTHS.between(start, end), is(4));
    }

    @Test
    public void executeCodeDemo() throws ParseException {
        ChronoFormatter<HijriCalendar> formatter =
            ChronoFormatter.setUp(HijriCalendar.class, Locale.ENGLISH)
                .addPattern("EEE, d. MMMM yy", PatternType.NON_ISO_DATE).build()
                .withCalendarVariant(HijriCalendar.VARIANT_UMALQURA)
                .with(Attributes.PIVOT_YEAR, 1500); // mapped to range 1400-1499
        HijriCalendar hijri = formatter.parse("Thu, 29. Ramadan 36");
        PlainDate date = hijri.transform(PlainDate.class);
        System.out.println(date); // 2015-07-16
    }

    @Test
    public void executeICU() throws ParseException {
        ChronoFormatter<HijriCalendar> formatter =
            ChronoFormatter.setUp(HijriCalendar.class, Locale.ENGLISH)
                .addPattern("y-MM-dd", PatternType.NON_ISO_DATE).build()
                .withCalendarVariant(HijriCalendar.VARIANT_ICU4J);
        HijriCalendar hijri = formatter.parse("1-01-01");
        PlainDate date = hijri.transform(PlainDate.class);
        System.out.println(date); // 622-07-18
    }

}