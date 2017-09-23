package com.debugarcade.spelling;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Scanner;
import pt.tumba.spell.DefaultWordFinder;
import pt.tumba.spell.SpellChecker;

/**
 *
 * @author arawson
 */
public class TestHasher {

	private static final String[] TEST_WORDS = {
		"yes", "no", "play"
	};

	private static final String[] TEST_INPUTS = {
		"aaron", "memeweaver", "yes", "no", "maybe", "vote", "poll", "play",
		"yse", "on", "myabe", "mayeb", "vtoe", "voet", "voat", "plol", "opll",
		"paly", "ply"
	};

	public static void main(String[] args) throws IOException {
		SpellChecker spellCheck = new SpellChecker();
		File dict = new File("./assets/jaspell-dict/english.txt").getCanonicalFile();
		File misspells = new File("./assets/jaspell-dict/common-misspells.txt").getCanonicalFile();
		File jargon = new File("./assets/jaspell-dict/jargon.txt").getCanonicalFile();
		
		try {
			spellCheck.initialize(
					dict.getAbsolutePath(),
					misspells.getAbsolutePath(),
					jargon.getAbsolutePath());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		DefaultWordFinder finder = new DefaultWordFinder();

		String line;
		line = "paly";

		finder.setText(line);
		String aux = null;
		while ((aux = finder.next()) != null) {
			String aux2 = spellCheck.findMostSimilar(aux, true);
			if (aux2 != null) {
				finder.replace(aux2);
			}
		}

		System.out.println(finder.getText());
	}
}
