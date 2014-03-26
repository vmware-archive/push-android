package org.omnia.pushsdk.sample.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Collection of utility methods to assist with String logic.
 */
public class StringUtil {

	/**
	 * Checks if the `string` is empty.
	 * 
	 * @param string
	 *            a `string` to validate
	 * @return True if the string is `null` or has a length of 0. False otherwise.
	 */
	public static boolean isBlank(final String string) {
		return isEmpty(string);
	}

	/**
	 * Checks if the `string` is empty.
	 * 
	 * @param input
	 *            a `string` to validate
	 * @return True if the string is `null` or has a length of 0. False otherwise.
	 */
	public static boolean isEmpty(final String input) {
		return input == null || input.length() <= 0;
	}

	/**
	 * Checks if the `string` is not empty.
	 * 
	 * @param input
	 *            a `string` to validate
	 * @return True if the string not `null` or has a length greater than zero.
	 */
	public static boolean isNotEmpty(final String input) {
		return !isEmpty(input);
	}

	/**
	 * Joins all items in the `collection` into a `string`, separated by the given `delimiter`.
	 * 
	 * @param collection
	 *            the collection of items
	 * @param delimiter
	 *            the delimiter to insert between each item
	 * @return the string representation of the collection of items
	 */
	public static String join(Collection<?> collection, String delimiter) {
		if (collection == null)
			return "";

		StringBuilder buffer = new StringBuilder();
		Iterator<?> iter = collection.iterator();
		while (iter.hasNext()) {
			buffer.append(iter.next());
			if (iter.hasNext()) {
				buffer.append(delimiter);
			}
		}
		return buffer.toString();
	}

	/**
	 * Joins all items in the `array` into a `string`, separated by the given `delimiter`.
	 * 
	 * @param array
	 *            the collection of items
	 * @param delimiter
	 *            the delimiter to insert between each item
	 * @return the string representation of the collection of items
	 */
	public static String join(Object[] array, String delimiter) {
		if (array == null)
			return "";

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < array.length; i += 1) {
			if (i > 0) {
				buffer.append(delimiter);
			}
			buffer.append(array[i].toString());
		}
		return buffer.toString();
	}

	/**
	 * Returns the leftmost `n` characters of the string `s`, or the whole string if the string's length is less than
	 * `n`. If the string is `null` then an empty string is returned.
	 * 
	 * @param s
	 *            the string to truncate
	 * @param n
	 *            the maximum number of characters to return
	 * @return the first n character of s, or the whole string if the string's length is less than n
	 */
	public static String left(final String s, final int n) {
		if (s != null) {
			return s.substring(0, Math.min(s.length(), n));
		} else {
			return "";
		}
	}

	/**
	 * Returns the rightmost `n` characters of the string `s`, or the whole string if the string's length is less than
	 * n. If the string is `null` then an empty string is returned.
	 * 
	 * @param s
	 *            the string to truncate
	 * @param n
	 *            the maximum number of characters to return
	 * @return the last n characters of s, or the whole string if the string's length is less than n. If the string is
	 *         null, then an empty string is returned.
	 */
	public static String right(final String s, final int n) {
		if (s != null) {
			final int length = s.length();
			return s.substring(length - Math.min(length, n), length);
		} else {
			return "";
		}
	}

	/**
	 * Appends the string `append` to `string` separated by the `delimiter` string.
	 * 
	 * @param string
	 *            the string in front
	 * @param append
	 *            the string to append
	 * @param delimiter
	 *            the string that separates `string` and `append`
	 * @return appended string of `string` + `delimiter` + `append`, or `null` if `string` if `string` is `null`.
	 */
	public static String append(final String string, final String append, final String delimiter) {
		if (string == null) {
			return append;
		} else {
			final StringBuilder builder = new StringBuilder(string);
			if (delimiter != null)
				builder.append(delimiter);
			if (append != null)
				builder.append(append);
			return builder.toString();
		}
	}

	/**
	 * Breaks a string of numbers delimited by "," into a list of integers
	 * 
	 * @param string
	 *            The string containing a list of numbers
	 * @return list of integers contained in the string
	 */
	public static List<Integer> getListOfIntegersFromString(String string) {
		final List<Integer> l = new ArrayList<Integer>();
		if (string.trim().length() <= 0)
			return l;
		final String[] tokens = string.split(",");
		for (String t : tokens) {
			try {
				l.add(Integer.parseInt(t.trim()));
			} catch (NumberFormatException e) {
				PushLibLogger.ex(e);
			}
		}
		return l;
	}

	/**
	 * Breaks the string `s`, separated by `delimiter` into individual string tokens.
	 * 
	 * @param s
	 *            the string to separate
	 * @param delimiter
	 *            the delimiter to use
	 * @return a list of strings
	 */
	public static List<String> getListOfStringsFromString(final String s, final String delimiter) {
		final List<String> l = new ArrayList<String>();
		if (s.trim().length() <= 0)
			return l;
		final String[] tokens = s.split(delimiter);
		for (final String t : tokens) {
			l.add(t.trim());
		}
		return l;
	}

	/**
	 * Breaks the string `a`, separated by `delimiter` into a `Set` of individual integers.
	 * 
	 * @param s
	 *            the string to separate
	 * @param delimiter
	 *            the delimiter to use
	 * @return a set of integers
	 */
	public static Set<Integer> getSetOfIntegersFromString(final String s, final String delimiter) {
		if (s != null && delimiter != null) {
			final String[] unfiltered = s.split(delimiter);

			final Set<Integer> set = new TreeSet<Integer>();
			for (final String t : unfiltered) {
				try {
					set.add(Integer.parseInt(t.trim()));
				} catch (final NumberFormatException e) {
					PushLibLogger.ex(e);
				}
			}
			return set;
		}
		return null;
	}

	/**
	 * Takes in a string of words `s` separated by `delimiter`, and capitalizes each word
	 * 
	 * @param s
	 *            The string containing the words to be capitalized
	 * @param delimiter
	 *            The string that delimits the words in the string `s`
	 * @return string of capitalized words from `s`, or empty string if `s` is `null`.
	 */
	public static String capitalizeWords(final String s, final String delimiter) {
		if (s == null)
			return "";

		final String[] inWords;
		if (delimiter != null) {
			inWords = s.split(delimiter);
		} else {
			inWords = new String[] { s };
		}
		final List<String> outWords = new ArrayList<String>();
		for (final String word : inWords) {
			if (word.length() == 0) {
				outWords.add("");
			} else if (word.length() == 1) {
				outWords.add(word.toUpperCase(Locale.getDefault()));
			} else if (word.length() > 1) {
				outWords.add(word.substring(0, 1).toUpperCase(Locale.getDefault()) + word.substring(1).toLowerCase(Locale.getDefault()));
			}
		}
		return join(outWords, delimiter);
	}

	/**
	 * Takes a string with JSON-formatted text and converts it to a Map<String, String>. This method assumes that the
	 * JSON contains exactly one JSON object with key-value pairs. All keys and values are strings.
	 * 
	 * @param string
	 *            a string with JSON text consisting of one JSON object with key-values pairs. e.g.: {"k1":"v1",
	 *            "k2":"v2"}
	 * 
	 * @return a Map<String,String> with one item for each of the item in the JSON object. Returns null if the input
	 *         string is null. Returns an empty Map if the JSON object contains no items.
	 * 
	 * @throws JSONException
	 *             if the String contains invalid JSON
	 */
	public static Map<String, String> getMapForJsonString(String string) throws JSONException {
		if (string == null) {
			return null;
		}
		final JSONObject json = new JSONObject(string);
		final Map<String, String> result = new HashMap<String, String>();
		JSONArray names = json.names();
		if (names != null) {
			for (int i = 0; i < names.length(); i += 1) {
				String name = names.getString(i);
				result.put(name, json.getString(name));
			}
		}
		return result;
	}

}
