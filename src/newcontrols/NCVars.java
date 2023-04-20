package newcontrols;

import mindustry.Vars;
import newcontrols.ui.fragments.AIPanel;

public class NCVars {
	public static AIPanel aipanel = new AIPanel();

	public static void init() {
		aipanel.build(Vars.ui.hudGroup);
	}
}
