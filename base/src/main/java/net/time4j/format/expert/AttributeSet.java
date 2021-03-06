/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2018 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (AttributeSet.java) is part of project Time4J.
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

package net.time4j.format.expert;

import net.time4j.base.ResourceLoader;
import net.time4j.engine.AttributeKey;
import net.time4j.engine.AttributeQuery;
import net.time4j.engine.ChronoCondition;
import net.time4j.engine.ChronoDisplay;
import net.time4j.engine.Chronology;
import net.time4j.format.Attributes;
import net.time4j.format.Leniency;
import net.time4j.format.NumberSymbolProvider;
import net.time4j.format.NumberSystem;
import net.time4j.format.OutputContext;
import net.time4j.format.TextWidth;
import net.time4j.format.internal.FormatUtils;
import net.time4j.format.internal.LanguageMatch;
import net.time4j.i18n.SymbolProviderSPI;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * <p>A decorator for standard format attributes. </p>
 *
 * @author  Meno Hochschild
 * @since   3.0
 */
final class AttributeSet
    implements AttributeQuery {

    //~ Statische Felder/Initialisierungen --------------------------------

    static final AttributeKey<String> PLUS_SIGN = Attributes.createKey("PLUS_SIGN", String.class);
    static final AttributeKey<String> MINUS_SIGN = Attributes.createKey("MINUS_SIGN", String.class);

    private static final NumberSymbolProvider NUMBER_SYMBOLS;

    static {
        NumberSymbolProvider p = null;
        int count = 0;

        for (NumberSymbolProvider tmp : ResourceLoader.getInstance().services(NumberSymbolProvider.class)) {
            int size = tmp.getAvailableLocales().length;
            if (size > count) {
                p = tmp;
                count = size;
            }
        }

        if (p == null) {
            p = SymbolProviderSPI.INSTANCE;
        }

        NUMBER_SYMBOLS = p;
    }

    private static final char ISO_DECIMAL_SEPARATOR = (
        Boolean.getBoolean("net.time4j.format.iso.decimal.dot")
        ? '.'
        : ',' // Empfehlung des ISO-Standards
    );

    private static final ConcurrentMap<String, NumericalSymbols> NUMBER_SYMBOL_CACHE =
        new ConcurrentHashMap<>();
    private static final NumericalSymbols DEFAULT_NUMERICAL_SYMBOLS =
        new NumericalSymbols(NumberSystem.ARABIC, '0', ISO_DECIMAL_SEPARATOR, "+", "-");

    //~ Instanzvariablen --------------------------------------------------

    private final Map<String, Object> internals;
    private final Attributes attributes;
    private final Locale locale;
    private final int level; // Ebene der optionalen Verarbeitungshierarchie
    private final int section; // Identifiziert eine optionale Attributsektion
    private final ChronoCondition<ChronoDisplay> printCondition; // nullable

    //~ Konstruktoren -----------------------------------------------------

    AttributeSet(
        Attributes attributes,
        Locale locale
    ) {
        this(attributes, locale, 0, 0, null);

    }

    AttributeSet(
        Attributes attributes,
        Locale locale,
        int level,
        int section,
        ChronoCondition<ChronoDisplay> printCondition
    ) {
        super();

        if (attributes == null) {
            throw new NullPointerException("Missing format attributes.");
        }

        this.attributes = attributes;
        this.locale = ((locale == null) ? Locale.ROOT : locale);
        this.level = level;
        this.section = section;
        this.printCondition = printCondition;

        this.internals = Collections.emptyMap();

    }

    private AttributeSet(
        Attributes attributes,
        Locale locale,
        int level,
        int section,
        ChronoCondition<ChronoDisplay> printCondition,
        Map<String, Object> internals
    ) {
        super();

        if (attributes == null) {
            throw new NullPointerException("Missing format attributes.");
        }

        this.attributes = attributes;
        this.locale = ((locale == null) ? Locale.ROOT : locale);
        this.level = level;
        this.section = section;
        this.printCondition = printCondition;
        this.internals = Collections.unmodifiableMap(internals);

    }

    //~ Methoden ----------------------------------------------------------

    @Override
    public boolean contains(AttributeKey<?> key) {

        if (this.internals.containsKey(key.name())) {
            return true;
        }

        return this.attributes.contains(key);

    }

    @Override
    public <A> A get(AttributeKey<A> key) {

        if (this.internals.containsKey(key.name())) {
            return key.type().cast(this.internals.get(key.name()));
        }

        return this.attributes.get(key);

    }

    @Override
    public <A> A get(
        AttributeKey<A> key,
        A defaultValue
    ) {

        if (this.internals.containsKey(key.name())) {
            return key.type().cast(this.internals.get(key.name()));
        }

        return this.attributes.get(key, defaultValue);

    }

    /**
     * <p>Compares all internal format attributes. </p>
     */
    /*[deutsch]
     * <p>Vergleicht auf Basis aller internen Formatattribute. </p>
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj instanceof AttributeSet) {
            AttributeSet that = (AttributeSet) obj;
            return (
                this.attributes.equals(that.attributes)
                && this.locale.equals(that.locale)
                && (this.level == that.level)
                && (this.section == that.section)
                && isEqual(this.printCondition, that.printCondition)
                && this.internals.equals(that.internals)
            );
        } else {
            return false;
        }

    }

    /*[deutsch]
     * <p>Berechnet den Hash-Code. </p>
     */
    @Override
    public int hashCode() {

        return 7 * this.attributes.hashCode() + 37 * this.internals.hashCode();

    }

    /**
     * <p>Supports mainly debugging. </p>
     */
    /*[deutsch]
     * <p>Dient vorwiegend der Debugging-Unterst&uuml;tzung. </p>
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName());
        sb.append("[attributes=");
        sb.append(this.attributes);
        sb.append(",locale=");
        sb.append(this.locale);
        sb.append(",level=");
        sb.append(this.level);
        sb.append(",section=");
        sb.append(this.section);
        sb.append(",print-condition=");
        sb.append(this.printCondition);
        sb.append(",other=");
        sb.append(this.internals);
        sb.append(']');
        return sb.toString();

    }

    Attributes getAttributes() {

        return this.attributes;

    }

    Locale getLocale() {

        return this.locale;

    }

    int getLevel() {

        return this.level;

    }

    int getSection() {

        return this.section;

    }

    ChronoCondition<ChronoDisplay> getCondition() {

        return this.printCondition; // nullable

    }

    static AttributeSet createDefaults(
        Chronology<?> chronology,
        Attributes attributes,
        Locale locale
    ) {

        Attributes.Builder builder = new Attributes.Builder(chronology);
        builder.set(Attributes.LENIENCY, Leniency.SMART);
        builder.set(Attributes.TEXT_WIDTH, TextWidth.WIDE);
        builder.set(Attributes.OUTPUT_CONTEXT, OutputContext.FORMAT);
        builder.set(Attributes.PAD_CHAR, ' ');
        builder.setAll(attributes);
        AttributeSet as = new AttributeSet(builder.build(), locale);
        return as.withLocale(locale);

    }

    /**
     * <p>Setzt die Attribute neu. </p>
     *
     * @param   attributes  new format attributes
     */
    AttributeSet withAttributes(Attributes attributes) {

        return new AttributeSet(attributes, this.locale, this.level, this.section, this.printCondition, this.internals);

    }

    /**
     * <p>Setzt das angegebene interne Attribut neu. </p>
     *
     * @param   <A> generic type of attribute value
     * @param   key     attribute key
     * @param   value   attribute value (if {@code null} then the attribute will be removed else inserted or updated)
     * @return  changed attribute set
     * @since   3.11/4.8
     */
    <A> AttributeSet withInternal(
        AttributeKey<A> key,
        A value
    ) {

        Map<String, Object> map = new HashMap<>(this.internals);
        if (value == null) {
            map.remove(key.name());
        } else {
            map.put(key.name(), value);
        }
        return new AttributeSet(this.attributes, this.locale, this.level, this.section, this.printCondition, map);

    }

    /**
     * <p>Setzt die Sprach- und L&auml;ndereinstellung. </p>
     *
     * <p>Die Attribute {@link Attributes#NUMBER_SYSTEM}, {@link Attributes#ZERO_DIGIT},
     * {@link Attributes#DECIMAL_SEPARATOR} und {@link Attributes#LANGUAGE} werden automatisch mit angepasst. </p>
     *
     * @param   locale      new language and country setting
     * @return  this instance for method chaining
     * @throws  IllegalArgumentException if an unicode extension is not recognized
     */
    AttributeSet withLocale(Locale locale) {

        Attributes.Builder builder = new Attributes.Builder();
        builder.setAll(this.attributes);
        String plus;
        String minus;

        String lang = LanguageMatch.getAlias(locale);
        String country = FormatUtils.getRegion(locale);

        if (lang.equals("zh") && locale.getScript().equals("Hant")) {
            country = "TW";
        }

        if (lang.isEmpty() && country.isEmpty()) {
            locale = Locale.ROOT;
            builder.set(Attributes.NUMBER_SYSTEM, NumberSystem.ARABIC);
            builder.set(Attributes.DECIMAL_SEPARATOR, ISO_DECIMAL_SEPARATOR);
            plus = "+";
            minus = "-";
        } else {
            String key = lang;

            if (!country.isEmpty()) {
                key = key + "_" + country;
            }

            NumericalSymbols symbols = NUMBER_SYMBOL_CACHE.get(key);

            if (symbols == null) {
                try {
                    symbols =
                        new NumericalSymbols(
                            NUMBER_SYMBOLS.getDefaultNumberSystem(locale),
                            NUMBER_SYMBOLS.getZeroDigit(locale),
                            NUMBER_SYMBOLS.getDecimalSeparator(locale),
                            NUMBER_SYMBOLS.getPlusSign(locale),
                            NUMBER_SYMBOLS.getMinusSign(locale)
                        );
                } catch (RuntimeException re) {
                    symbols = DEFAULT_NUMERICAL_SYMBOLS;
                }

                NumericalSymbols old =
                    NUMBER_SYMBOL_CACHE.putIfAbsent(key, symbols);
                if (old != null) {
                    symbols = old;
                }
            }

            String code = locale.getUnicodeLocaleType("nu");
            char zeroChar = symbols.zeroDigit;
            NumberSystem numsys;

            if (code == null) {
                numsys = symbols.defnumsys;
            } else {
                numsys = null;
                for (NumberSystem ns : NumberSystem.values()) {
                    if (code.equals(ns.getCode())) {
                        numsys = ns;
                        if (numsys.isDecimal()) {
                            zeroChar = numsys.getDigits().charAt(0);
                        }
                        break;
                    }
                }
                if (numsys == null) {
                    throw new IllegalArgumentException("Unsupported unicode-nu-extension: " + code);
                }
            }

            builder.set(Attributes.NUMBER_SYSTEM, numsys);
            builder.set(Attributes.ZERO_DIGIT, zeroChar);
            builder.set(Attributes.DECIMAL_SEPARATOR, symbols.decimalSeparator);
            plus = symbols.plus;
            minus = symbols.minus;
        }

        builder.setLanguage(locale);
        Map<String, Object> newInternals = new HashMap<>(this.internals);
        newInternals.put(PLUS_SIGN.name(), plus);
        newInternals.put(MINUS_SIGN.name(), minus);
        return new AttributeSet(builder.build(), locale, this.level, this.section, this.printCondition, newInternals);

    }

    // used to create merged global attributes for a new formatter
    static AttributeSet merge(
        AttributeSet outer,
        AttributeSet inner
    ) {

        Map<String, Object> internalsNew = new HashMap<>();
        internalsNew.putAll(inner.internals);
        internalsNew.putAll(outer.internals);
        Attributes attributesNew =
            new Attributes.Builder()
                .setAll(inner.attributes)
                .setAll(outer.attributes)
                .build();
        AttributeSet as = new AttributeSet(attributesNew, Locale.ROOT, 0, 0, null, internalsNew);
        return as.withLocale(outer.locale);

    }

    private static boolean isEqual(Object o1, Object o2) {

        return ((o1 == null) ? (o2 == null) : o1.equals(o2));

    }

    //~ Innere Klassen ----------------------------------------------------

    private static class NumericalSymbols {

        //~ Instanzvariablen ----------------------------------------------

        private final NumberSystem defnumsys;
        private final char zeroDigit;
        private final char decimalSeparator;
        private final String plus;
        private final String minus;

        //~ Konstruktoren -------------------------------------------------

        NumericalSymbols(
            NumberSystem defnumsys,
            char zeroDigit,
            char decimalSeparator,
            String plus,
            String minus
        ) {
            super();

            this.defnumsys = defnumsys;
            this.zeroDigit = zeroDigit;
            this.decimalSeparator = decimalSeparator;
            this.plus = plus;
            this.minus = minus;

        }

    }

}
