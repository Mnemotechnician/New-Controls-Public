package newcontrols;

import arc.*;
import mindustry.*;
import mindustry.game.*;

import newcontrols.ui.fragments.*;

public class NCVars {
	public static AIPanel aipanel = new AIPanel();

	public static void init() {
		aipanel.build(Vars.ui.hudGroup);
	}
}
