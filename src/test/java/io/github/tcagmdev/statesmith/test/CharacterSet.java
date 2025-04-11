package io.github.tcagmdev.statesmith.test;

import java.util.HashSet;

public class CharacterSet extends HashSet<Character> {
	public static CharacterSet fromString(String string) {
		CharacterSet result = new CharacterSet();

		for (char c : string.toCharArray()) result.add(c);

		return result;
	}
}