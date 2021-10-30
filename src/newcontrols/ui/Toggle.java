package newcontrols.ui;

import arc.*;
import arc.func.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.utils.*;
import mindustry.gen.*;
import mindustry.ui.*;

public class Toggle extends TextButton {
	
	/** I don't like this approach too but something's wrong with Button.isChecked as it returns false for no reason. */
	public boolean enabled = false;
	
	public Toggle(String text, boolean enabled, Boolc cons) {
		super(text, Styles.clearTogglet);
		clicked(() -> {
			setChecked(this.enabled = !this.enabled);
			cons.get(this.enabled);
		});
		
		this.enabled = enabled;
	}

}