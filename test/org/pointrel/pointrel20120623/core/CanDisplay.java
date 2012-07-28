package org.pointrel.pointrel20120623.core;

import java.awt.*;

// From: http://www.experts-exchange.com/Programming/Languages/Java/Q_21226672.html
public class CanDisplay {
	static String currentFontName = "";

	public static void main(String[] args) {
		// TODO hardcoding
		args = new String[] {"2388"};
		if (args.length < 1) {
			// 	9096 	0x2388
			System.out.println("Usage: java CanDisplay <Unicode character, e.g. 0044>");
			System.exit(-1);
		}
		char c = (char) Integer.parseInt(args[0], 16);
		System.out.println("Checking on character " + c);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font[] fonts = ge.getAllFonts();

		for (int i = 0; i < fonts.length; i++) {
			String fontName = fonts[i].getFontName();
			/**
			 * Assume that if a glyph is contained in a certain typeface, then
			 * its first font will be able to display it. Otherwise this
			 * iteration can take a long time.
			 */
			if (fontName.equals(currentFontName)) {
				continue;
			} else {
				currentFontName = fontName;
			}
			if (fonts[i].canDisplay(c)) {
				System.out.println(fonts[i]);
				// Just take the first one as this iteration can take a long
				// time!
				// TODO: not stopping
				// System.exit(0);
			}
		}
		// TODO: System.out.println("Font not found");
		System.out.println("Done");
	}

}
