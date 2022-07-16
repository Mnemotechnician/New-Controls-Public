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
	public Func<Toggle, Boolean> toggleProv;
	
	public Toggle(String text, boolean enabled, Boolc cons) {
		super(text, Styles.fullTogglet);
		clicked(() -> {
			setChecked(this.enabled = !this.enabled);
			cons.get(this.enabled);
		});
		
		this.enabled = enabled;
	}

	public Toggle(String text, Func<Toggle, Boolean> enabled, Boolc cons) {
		this(text, false, cons);
		toggle(enabled);
		this.enabled = enabled.get(this);
	}

	@Override
	public void act(float delta) {
		super.act(delta);

		if (toggleProv != null) {
			boolean state = toggleProv.get(this);
			enabled = state;
			setChecked(state);
		}
	}

	public Toggle toggle(Func<Toggle, Boolean> func) {
		toggleProv = func;
		return this;
	}
}
