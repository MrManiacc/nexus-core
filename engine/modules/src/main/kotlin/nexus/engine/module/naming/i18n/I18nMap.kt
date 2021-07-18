package nexus.engine.module.naming.i18n

import com.google.common.collect.ImmutableMap
import java.util.*
import javax.annotation.concurrent.Immutable

/**
 * I18nMap is a map of Strings by Locale, to support lookup fields that can have different values for different languages (internationalized fields).
 *
 *
 * This is not intended to be used as a replacement of Java's existing internationalisation support for strings, but instead used for
 * internationalised strings read from external sources.
 *
 *
 *
 * If a String is not available for a particular locale, then it will fallback though the following steps to find one:
 *
 *
 *  1. Drop Locale variant
 *  1. Drop Locale country
 *  1. Use system default display Locale
 *  1. Use English Locale
 *  1. Use any available Locale
 *
 *
 * @author Immortius
 */
@Immutable
class I18nMap : Iterable<Map.Entry<Locale, String>> {
    private val values: Map<Locale, String>

    /**
     * Constructor when a mapping of Locale to String is available.
     *
     * @param values A map of locale-string values.
     */
    constructor(values: Map<Locale, String>) {
        this.values = ImmutableMap.copyOf(values)
    }

    /**
     * Constructor when only a single String is available - this will be registered against the default Locale.
     *
     * @param value The sole value of this map.
     */
    constructor(value: String) {
        values = ImmutableMap.of(Locale.getDefault(Locale.Category.DISPLAY), value)
    }

    /**
     * @return The most appropriate string value to use based on the system default Locale
     */
    fun value(): String {
        var result = values[Locale.getDefault(Locale.Category.DISPLAY)]
        if (result == null) {
            result = values[Locale.ENGLISH]
        }
        if (result == null && !values.isEmpty()) {
            result = values.values.iterator().next()
        }
        if (result == null) {
            result = ""
        }
        return result
    }

    /**
     * @param locale The locale to get the string value for
     * @return The most appropriate string value for the given locale.
     */
    fun valueFor(locale: Locale): String {
        var result = values[locale]
        if (result == null && !locale.variant.isEmpty()) {
            val fallbackLocale = Locale(locale.language, locale.country)
            result = values[fallbackLocale]
        }
        if (result == null && !locale.country.isEmpty()) {
            val fallbackLocale = Locale(locale.language)
            result = values[fallbackLocale]
        }
        if (result == null) {
            result = value()
        }
        return result
    }

    /**
     * @return The most appropriate string value based on the system default locale.
     */
    override fun toString(): String {
        return value()
    }

    override fun iterator(): Iterator<Map.Entry<Locale, String>> {
        return values.entries.iterator()
    }

    override fun hashCode(): Int {
        return Objects.hash(values)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is I18nMap) {
            return values == other.values
        }
        return false
    }
}
