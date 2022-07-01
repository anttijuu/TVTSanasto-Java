package fi.oulu.tol;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;

import java.io.File;
import java.io.IOException;

import fi.oulu.tol.model.Language;

public class Settings {
	private Settings() {
	}

	public static int WINDOW_WIDTH = 1200;
	public static int WINDOW_HEIGHT = 800;
	public static int LIST_WIDTH = 200;
	public static Language language = Language.FINNISH;

	public static Font emojiFont;

	public static void installEmojiFont() throws IOException, FontFormatException {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font font = Font.createFont(Font.TRUETYPE_FONT, new File("NotoEmoji-VariableFont_wght.ttf"));
		emojiFont = font.deriveFont(16.0f);
		ge.registerFont(emojiFont);
	}
}
