package newcontrols;

import io.mnemotechnician.autoupdater.*;
import arc.*;
import arc.util.*;
import arc.struct.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;

import newcontrols.*;
import newcontrols.util.*;

public class NCMod extends Mod {
	
	public NCMod() {
		NCVars.init();
		//NCSpying.init();
		
		Events.on(EventType.ClientLoadEvent.class, a -> {
			Updater.checkUpdates(this);
		});
	}
	
	@Override
	public void loadContent(){
		
	}
}
