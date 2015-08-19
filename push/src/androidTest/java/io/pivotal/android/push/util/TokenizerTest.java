package io.pivotal.android.push.util;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import java.util.Arrays;
import java.util.List;

public class TokenizerTest extends AndroidTestCase {

	public void testNull() {
		try {
			Tokenizer.tokenize(null);
		} catch (IllegalArgumentException e) {
			return;
		}
		fail();
	}

	public void test0() {
		// NOTE: all delimiters collapse together
		assertEquals(makeList(), Tokenizer.tokenize(""));
		assertEquals(makeList(), Tokenizer.tokenize("."));
		assertEquals(makeList(), Tokenizer.tokenize("-"));
		assertEquals(makeList(), Tokenizer.tokenize("-.-"));
		assertEquals(makeList(), Tokenizer.tokenize(" "));
		assertEquals(makeList(), Tokenizer.tokenize(" - -"));
	}

	public void test1() {
		assertEquals(makeList("1"), Tokenizer.tokenize("1"));
	}

	public void test2() {
		assertEquals(makeList("1", "0"), Tokenizer.tokenize("1.0"));
	}

	public void test3() {
		assertEquals(makeList("10"), Tokenizer.tokenize("10"));
	}

	public void test4() {
		// NOTE: all delimiters are treated the same as each other
		assertEquals(makeList("10", "1"), Tokenizer.tokenize("10.1"));
		assertEquals(makeList("10", "1"), Tokenizer.tokenize("10-1"));
		assertEquals(makeList("10", "1"), Tokenizer.tokenize("10 1"));
	}

	public void test5() {
		assertEquals(makeList("1", "10"), Tokenizer.tokenize("1.10"));
		assertEquals(makeList("1", "10"), Tokenizer.tokenize("1-10"));
		assertEquals(makeList("1", "10"), Tokenizer.tokenize("1 10"));
	}

	public void test6() {
		assertEquals(makeList("1", "a"), Tokenizer.tokenize("1a"));
	}

	public void test7() {
		assertEquals(makeList("1", "a"), Tokenizer.tokenize("1.a"));
		assertEquals(makeList("1", "a"), Tokenizer.tokenize("1.a"));
	}

	public void test8() {
		assertEquals(makeList("1", "a", "2"), Tokenizer.tokenize("1.a2"));
		assertEquals(makeList("1", "a", "2"), Tokenizer.tokenize("1a.2"));
		assertEquals(makeList("1", "a", "2"), Tokenizer.tokenize("1a2"));
	}

	public void test9() {
		assertEquals(makeList("1", "a", "b", "23"), Tokenizer.tokenize("1.a.b23"));
		assertEquals(makeList("1", "ab", "2", "3"), Tokenizer.tokenize("1ab.2.3"));
		assertEquals(makeList("1", "a", "b", "23"), Tokenizer.tokenize("1a.b23"));
		assertEquals(makeList("1", "a", "b", "23"), Tokenizer.tokenize("1a...-b23"));
		assertEquals(makeList("1", "a", "b", "2", "3"), Tokenizer.tokenize("1-a..b2.3."));
	}

	public void test10() {
		assertEquals(makeList("1", "A", "*", "^", "b"), Tokenizer.tokenize("1.A-*-^.b"));
		assertEquals(makeList("1", "A*^b"), Tokenizer.tokenize("1.A*^b"));
	}

	public void test11() {
		assertEquals(makeList("a", "1"), Tokenizer.tokenize(".a1"));
		assertEquals(makeList("a", "1"), Tokenizer.tokenize(".-a1"));
		assertEquals(makeList("a", "1"), Tokenizer.tokenize(".-.-a-1"));
		assertEquals(makeList("1", "2", "ab"), Tokenizer.tokenize("-1.2-ab"));
	}

	public void test12() {
		assertEquals(makeList("hello", "world"), Tokenizer.tokenize("hello.-world"));
		assertEquals(makeList("hell", "0", "w", "0", "rld"), Tokenizer.tokenize("hell0w0rld"));
		assertEquals(makeList("1", "helloworld", "0234567"), Tokenizer.tokenize("...1helloworld--0234567."));
		assertEquals(makeList("1", "2", "hello^*", "12", "world*^"), Tokenizer.tokenize("1-2hello^*12world*^....-"));
	}

	public void test13() {
		// NOTE: minus signs are considered separators as oppose to negative signs
		MoreAsserts.assertNotEqual(makeList("-1", "a", "2"), Tokenizer.tokenize("-1.a2"));
		MoreAsserts.assertNotEqual(makeList("1", "a", "-2"), Tokenizer.tokenize("1a.-2"));
		MoreAsserts.assertNotEqual(makeList("-1", "a", "-2"), Tokenizer.tokenize("-1a.-2"));

	}

	private List<String> makeList(String... s) {
		return Arrays.asList(s);
	}

}
