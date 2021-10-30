package newcontrols.ui.fragments;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.type.*;
import mindustry.input.*;
import mindustry.entities.*;

import static arc.Core.*;
import static mindustry.Vars.*;

import newcontrols.ui.*;
import newcontrols.input.*;

public class ActionPanel {
	
	//landscape, displayed on the bottom
	public static void buildLandscape(Table origin, final AIInput input) {
		origin.collapser(table -> {
			table.table(dangerous -> {
				dangerous.defaults().height(55);
				
				dangerous.button("@newcontrols.manual.pay-enter", Styles.nodet, () -> payloadEnter()).width(120);
			}).row();
			
			table.table(generic -> {
				generic.defaults().height(55);
				
				generic.button("@newcontrols.manual.command", Styles.nodet, () -> {
					Call.unitCommand(player);
				}).width(120).disabled(b -> player.dead() || player.unit().type.commandLimit < 1);
				
				generic.button(makeRegion("newcontrols-arrow-up"), Styles.clearTogglei, () -> {
					player.boosting = !player.boosting;
				}).size(55).update(b -> b.setChecked(player.boosting && player.unit().type.canBoost));
			}).row();
			
			table.table(payloads -> {
				payloads.defaults().size(160, 55);
				
				payloads.button("@newcontrols.manual.pickup-unit", Styles.nodet, () -> unitPickup())
				.disabled(b -> !(player.unit() instanceof Payloadc));
				
				payloads.button("@newcontrols.manual.pickup-block", Styles.nodet, () -> buildPickup())
				.disabled(b -> !(player.unit() instanceof Payloadc));
				
				payloads.button("@newcontrols.manual.drop", Styles.nodet, () -> input.tryDropPayload())
				.disabled(b -> !(player.unit() instanceof Payloadc pay) || !pay.hasPayload());
			}).row();
		}, () -> !Core.graphics.isPortrait());
	}
	
	//portrait, displayed above right thumbstick
	public static void buildPortrait(Table origin, final AIInput input) {
		origin.collapser(table -> {
			table.table(dangerous -> {
				dangerous.defaults().size(50);
				
				dangerous.button(makeRegion("newcontrols-enter-payload"), Styles.nodei, () -> payloadEnter());
			}).row();
			
			table.table(generic -> {
				generic.defaults().size(50);
				
				generic.button(makeRegion("newcontrols-command"), Styles.nodei, () -> {
					Call.unitCommand(player);
				}).disabled(b -> player.dead() || player.unit().type.commandLimit < 1);
				
				generic.button(makeRegion("newcontrols-arrow-up"), Styles.clearTogglei, () -> {
					player.boosting = !player.boosting;
				}).disabled(b -> !player.unit().type.canBoost).update(b -> b.setChecked(player.boosting && player.unit().type.canBoost));
			}).row();
			
			table.table(payloads -> {
				payloads.defaults().size(50);
				
				payloads.button(makeRegion("newcontrols-pick-unit"), Styles.nodei, () -> unitPickup())
				.disabled(b -> !(player.unit() instanceof Payloadc));
				
				payloads.button(makeRegion("newcontrols-pick-building"), Styles.nodei, () -> buildPickup())
				.disabled(b -> !(player.unit() instanceof Payloadc));
				
				payloads.button(makeRegion("newcontrols-drop-payload"), Styles.nodei, () -> input.tryDropPayload())
				.disabled(b -> !(player.unit() instanceof Payloadc pay) || !pay.hasPayload());
			});
		}, () -> Core.graphics.isPortrait());
	}
	
	protected static void unitPickup() {
		Unit self = player.unit();
		if (!(self instanceof Payloadc pay)) return;
			
		Unit target = Units.closest(player.team(), self.x, self.y, 8f, u -> u != self && u.isGrounded() && pay.canPickup(u) && u.within(self, u.hitSize + 8f));
		
		if (target != null) Call.requestUnitPayload(player, target);
	}
	
	protected static void buildPickup() {
		Unit self = player.unit();
		if (!(self instanceof Payloadc pay)) return;
				
		Building target = self.tileOn().build;
			
		if (target != null && pay.canPickup(target)) {
			Call.requestBuildPayload(player, target);
		}
	}
	
	protected static void payloadEnter() { 
		Unit self = player.unit();
		
		Building build = world.buildWorld(self.x, self.y);
		if (self != null && build != null && self.team() == build.team && build.canControlSelect(self)) {
			Call.unitBuildingControlSelect(self, build);
		}
	}
	
	//todo: can i not?
	protected static TextureRegionDrawable makeRegion(String name) {
		return new TextureRegionDrawable(atlas.find(name));
	}
	
}