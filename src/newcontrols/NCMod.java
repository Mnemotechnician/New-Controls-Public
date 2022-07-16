package newcontrols;

import arc.Events;
import com.github.mnemotechnician.autoupdater.Updater;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import newcontrols.ui.NCStyles;

public class NCMod extends Mod {
	public NCMod() {
		Events.on(EventType.ClientLoadEvent.class, a -> {
			Updater.checkUpdates(this);
			NCStyles.init();
			NCVars.init();
		});
	}
}
