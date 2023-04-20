package newcontrols.ui;

import arc.graphics.Color;
import arc.scene.ui.TextButton.TextButtonStyle;
import mindustry.ui.Fonts;

import static mindustry.ui.Styles.*;

public class NCStyles {
	public static TextButtonStyle fullt;
	public static TextButtonStyle clearPartialt;

	public static void init() {
		fullt = new TextButtonStyle() {{
			font = Fonts.def;
			fontColor = Color.white;
			down = flatOver;
			up = black;
			over = flatOver;
			disabled = black;
			disabledFontColor = Color.gray;
		}};

		clearPartialt = new TextButtonStyle() {{
			font = Fonts.def;
			down = flatDown;
			up = black6;
			over = flatOver;
			disabled = black8;
			disabledFontColor = Color.lightGray;
		}};
	}
}
