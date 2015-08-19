package io.pivotal.android.push.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tokenizer {

	/**
	 * Tokenize a version string.
	 * 
	 * NOTES:
	 * 
	 * Tokens will be separated by the delimiters { ' ', '-', '.' } or by breaks between sets of consecutive letters and
	 * sets of consecutive numbers.
	 * 
	 * Consecutive delimiters, even of different types, will be collapsed together.
	 * 
	 * No tokens of zero-length will be produced.
	 * 
	 * Dashes are considered to be delimiters, not negative signs.
	 * 
	 * Punctuation characters are treated the same as letters.
	 * 
	 * @param s
	 *            A string to tokenize. May not be null otherwise IllegalArgumentException will be thrown.
	 * 
	 * @return A list of string tokens.
	 */
	public static List<String> tokenize(String s) {

		checkForNull(s);

		if (s.length() == 0) {
			return Collections.emptyList();
		}

		if (s.length() == 1) {
			if (isDelimiter(s.charAt(0))) {
				return Collections.emptyList();
			} else {
				return Arrays.asList(s);
			}
		}

		return tokenizeLongerString(s);
	}

	private static void checkForNull(String s) {
		if (s == null) {
			throw new IllegalArgumentException("s may not be null");
		}
	}

	private static List<String> tokenizeLongerString(String s) {
		TokenizerResultBuilder results = new TokenizerResultBuilder();
		char lastChar = s.charAt(0);

		if (!isDelimiter(lastChar)) {
			results.append(lastChar);
		}

		for (int i = 1; i < s.length(); i += 1) {

			char thisChar = s.charAt(i);

			if (isTwoDigits(lastChar, thisChar) || isTwoWordCharacters(lastChar, thisChar)) {
				results.append(thisChar);
			} else if (isSingleDelimiter(lastChar, thisChar)) {
				results.endWord();
			} else if (isWordAdjacentToDigit(lastChar, thisChar)) {
				results.endWord();
				results.append(thisChar);
			} else if (!isTwoDelimiters(lastChar, thisChar)) {
				results.append(thisChar);
			}

			lastChar = thisChar;
		}

		return results.getResults();
	}

	private static boolean isSingleDelimiter(char lastChar, char thisChar) {
		return !isDelimiter(lastChar) && isDelimiter(thisChar);
	}

	private static class TokenizerResultBuilder {

		private StringBuilder sb;
		private ArrayList<String> results;

		public TokenizerResultBuilder() {
			this.sb = new StringBuilder();
			this.results = new ArrayList<String>();
		}

		public void append(char c) {
			sb.append(c);
		}

		public void endWord() {
			results.add(sb.toString());
			sb = new StringBuilder();
		}

		public List<String> getResults() {
			if (sb.length() > 0) {
				results.add(sb.toString());
			}
			return results;
		}
	}

	private static boolean isWordAdjacentToDigit(char lastChar, char thisChar) {
		return isDigit(lastChar) && isWordCharacter(thisChar) || isWordCharacter(lastChar) && isDigit(thisChar);
	}

	private static boolean isTwoWordCharacters(char lastChar, char thisChar) {
		return isWordCharacter(lastChar) && isWordCharacter(thisChar);
	}

	private static boolean isTwoDelimiters(char lastChar, char thisChar) {
		return isDelimiter(lastChar) && isDelimiter(thisChar);
	}

	private static boolean isTwoDigits(char lastChar, char thisChar) {
		return isDigit(lastChar) && isDigit(thisChar);
	}

	private static boolean isDelimiter(char c) {
		return c == '.' || c == '-' || c == ' ';
	}

	private static boolean isWordCharacter(char c) {
		return !isDigit(c) && !isDelimiter(c);
	}

	private static boolean isDigit(char c) {
		return Character.isDigit(c);
	}

}
