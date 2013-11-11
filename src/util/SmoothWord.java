package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmoothWord {
	static String NUM = "*num*";

	static Pattern p1 = Pattern.compile("(^-{0,1}[0-9]+\\.*[0-9]*)+"); // eg -9,
																		// 100,
	// 100.001
	// etc
	static Pattern p2 = Pattern.compile("^-{0,1}[0-9]*\\.*[0-9]+"); // eg. -.5,
																	// .5
	static Pattern p3 = Pattern
			.compile("^-{0,1}[0-9]{1,3}[,[0-9]{3}]*\\.*[0-9]*"); // matches
	// 100,000
	static Pattern p4 = Pattern.compile("[0-9]+\\\\/[0-9]+"); // four \ needed,
	// java converts it
	// to \\
	static Pattern p5 = Pattern.compile("[0-9]+:[0-9]+"); // ratios and time
	static Pattern p6 = Pattern.compile("([0-9]+-)+[0-9]+"); // 1-2-3, 1-2-3-4
																// etc

	public static String smooth(String word) {
		//for nepali numbers: convert them to english
		for(int i = 0; i <= 9; i++) {
			  word = word.replace("" + (char)(0x966+i), "" + i);
		}
		Matcher m1 = p1.matcher(word);
		Matcher m2 = p2.matcher(word);
		Matcher m3 = p3.matcher(word);
		Matcher m4 = p4.matcher(word);
		Matcher m5 = p5.matcher(word);
		Matcher m6 = p6.matcher(word);

		if (m1.matches() || m2.matches() || m3.matches() || m4.matches()
				|| m5.matches() || m6.matches()) {
			word = NUM;
		}

		word = word.replaceAll("" + "([0-9]+\\\\/[0-9]+)|"
				+ "(([0-9]+-)+[0-9]+)|" + "([0-9]+:[0-9]+)|"
				+ "(^-{0,1}[0-9]{1,3}[,[0-9]{3}]*\\.*[0-9]*)|"
				+ "(^-{0,1}[0-9]*\\.*[0-9]+)|" + "(^-{0,1}[0-9]+\\.*[0-9]*)+",
				NUM); // for something like 10-years-old, 2-for-3 etc
		return word;
	}
}