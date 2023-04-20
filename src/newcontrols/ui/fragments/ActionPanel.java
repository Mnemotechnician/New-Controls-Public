package newcontrols.ui.fragments;

import arc.Core;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Table;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.ui.Styles;
import newcontrols.input.AIInput;
import newcontrols.ui.NCStyles;

import static arc.Core.*;
import static mindustry.Vars.*;

public class ActionPanel {
	//landscape, displayed on the bottom
	public static void buildLandscape(Table origin, final AIInput input) {
		origin.collapser(table -> {
			table.table(dangerous -> {
				dangerous.defaults().height(55);

				dangerous.button("@newcontrols.manual.pay-enter", NCStyles.clearPartialt, () -> payloadEnter()).width(120);

				dangerous.button(makeRegion("newcontrols-arrow-up"), Styles.cleari, () -> {
					player.boosting = !player.boosting;
				}).size(55).update(b -> b.setChecked(player.boosting && player.unit().type.canBoost));
			}).row();

			table.table(payloads -> {
				payloads.defaults().size(160, 55);

				payloads.button("@newcontrols.manual.pickup-unit", NCStyles.clearPartialt, () -> unitPickup())
					.disabled(b -> !(player.unit() instanceof Payloadc));

				payloads.button("@newcontrols.manual.pickup-block", NCStyles.clearPartialt, () -> buildPickup())
					.disabled(b -> !(player.unit() instanceof Payloadc));

				payloads.button("@newcontrols.manual.drop", NCStyles.clearPartialt, () -> input.tryDropPayload())
					.disabled(b -> !(player.unit() instanceof Payloadc) || !((Payloadc) player.unit()).hasPayload());
			}).row();
		}, () -> !graphics.isPortrait());
	}

	//portrait, displayed above right thumbstick
	public static void buildPortrait(Table origin, final AIInput input) {
		origin.defaults().pad(2f);

		origin.collapser(table -> {
			table.table(dangerous -> {
				dangerous.defaults().size(50).pad(2f);

				dangerous.button(makeRegion("newcontrols-enter-payload"), Styles.flati, () -> payloadEnter());

				dangerous.button(makeRegion("newcontrols-arrow-up"), Styles.clearTogglei, () -> {
					player.boosting = !player.boosting;
				}).disabled(b -> !player.unit().type.canBoost).update(b -> b.setChecked(player.boosting && player.unit().type.canBoost));

			}).row();

			table.table(payloads -> {
				payloads.defaults().size(50).pad(2f);

				payloads.button(makeRegion("newcontrols-pick-unit"), Styles.flati, () -> unitPickup())
					.disabled(b -> !(player.unit() instanceof Payloadc));

				payloads.button(makeRegion("newcontrols-pick-building"), Styles.flati, () -> buildPickup())
					.disabled(b -> !(player.unit() instanceof Payloadc));

				payloads.button(makeRegion("newcontrols-drop-payload"), Styles.flati, () -> input.tryDropPayload())
					.disabled(b -> !(player.unit() instanceof Payloadc) || !((Payloadc) player.unit()).hasPayload());
			});
		}, () -> Core.graphics.isPortrait());
	}

	protected static void unitPickup() {
		// todo migrate to kotlin if i'm ever going to continue this
		// because java sucks. imagine not understanding that [self] is a (Unit & Payloadc).
		if (!(player.unit() instanceof Payloadc)) return;
		Payloadc pay = (Payloadc) player.unit();
		Unit self = player.unit();

		Unit target = Units.closest(player.team(), self.x, self.y, 8f, u -> u != self && u.isGrounded() && pay.canPickup(u) && u.within(self, u.hitSize + 8f));

		if (target != null) Call.requestUnitPayload(player, target);
	}

	protected static void buildPickup() {
		Unit self = player.unit();
		if (!(self instanceof Payloadc)) return;

		Building target = self.tileOn().build;

		if (target != null && ((Payloadc) self).canPickup(target)) {
			Call.requestBuildPayload(player, target);
		}
	}

	protected static void payloadEnter() {
		Unit self = player.unit();

		Building build = world.buildWorld(self.x, self.y);
		if (build != null && self.team() == build.team && build.canControlSelect(self)) {
			Call.unitBuildingControlSelect(self, build);
		}
	}

	protected static TextureRegionDrawable makeRegion(String name) {
		return new TextureRegionDrawable(atlas.find(name));
	}
}
