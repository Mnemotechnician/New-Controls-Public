package newcontrols.ui.fragments;

import arc.*;
import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.type.*;
import mindustry.ui.fragments.*;
import mindustry.input.*;
import static arc.Core.*;

import newcontrols.input.*;
import newcontrols.input.AIInput.*;
import newcontrols.ui.*;
import newcontrols.ui.fragments.*;

//"why so many whitespaces?"
//Because this shit is unreadable without them!
public class AIPanel extends Fragment {
	
	public static float dsize = 65f;
	
	public boolean shown = false, enabled = false;
	
	InputHandler lastHandler = null;
	AIInput ai = new AIInput();
	
	@Override
	public void build(Group parent) {
		parent.fill(table -> {
			
			table.center().left();
			table.name = "ai-panel";
			
			table.button(Icon.wrench, Styles.clearTransi, () -> shown = !shown).size(dsize).left().row();
			
			table.table(Styles.black3, panel -> {
				
				panel.add("@newcontrols.ai.header").colspan(2).row();
				panel.table(h -> {
					
					h.add("@newcontrols.ai.status").padRight(5f);
					
					Prov<CharSequence> lText = () -> enabled ? (ai.manualMode ? "@newcontrols.ai.enabled-manual" : "@newcontrols.ai.enabled-ai") : "@newcontrols.ai.disabled";
					h.label(lText).height(60f).with(l -> {
						l.clicked(() -> {
							enabled = !enabled;
							
							if (enabled) {
								lastHandler = Vars.control.input;
								Vars.control.setInput(ai);
							} else if (lastHandler != null) {
								ai.finish();
								Vars.control.setInput(lastHandler);
							}
						});
						l.setStyle(Styles.techLabel);
					});
					
				}).padLeft(8f).row();
				
				panel.collapser(control -> {
					
					control.table(h -> {
						h.add("@newcontrols.ai.action").padRight(5f);
						h.label(() -> ai.toString()).marginBottom(5f).row();
					}).row();
					
					control.collapser(actions -> {
						
						actions.add("@newcontrols.ai.settings").row();
						//action selection
						actions.add((Element) new Spinner("@newcontrols.ai.actions-select", s -> {
							
							s.defaults().growX().height(40f);
							s.button("@newcontrols.ai.action-AUTO-TYPE", Styles.clearPartialt, () -> ai.current = AIAction.AUTO).row();
							s.button("@newcontrols.ai.action-ATTACK", Styles.clearPartialt, () -> ai.current = AIAction.ATTACK).row();
							s.button("@newcontrols.ai.action-MINE", Styles.clearPartialt, () -> ai.current = AIAction.MINE).row();
							s.button("@newcontrols.ai.action-PATROL", Styles.clearPartialt, () -> ai.current = AIAction.PATROL).row();
							
						})).growX().row();
						
						//auto actions
						actions.add((Element) new Spinner("@newcontrols.ai.actions-enable", s -> {
							
							s.defaults().growX().height(40f);
							s.add(new Toggle("@newcontrols.ai.action-ATTACK", true, enabled -> ai.attack = enabled)).row();
							s.add(new Toggle("@newcontrols.ai.action-MINE", true, enabled -> ai.mine = enabled)).row();
							s.add(new Toggle("@newcontrols.ai.action-PATROL", true, enabled -> ai.patrol = enabled)).row();
							
						})).growX().row();
						
						//preferences
						actions.add((Element) new Spinner("@newcontrols.ai.actions-preferences", s -> {
							
							s.defaults().growX().height(40f);
							
							//Attack range
							s.add(new NiceSlider("@newcontrols.ai.prefs.attack-radius", 0, 1200, 16, radius -> {
								ai.attackRadius = radius;
							})
							.max(() -> Vars.player.unit().type == null ? 1200 : Vars.player.unit().range() * 5)
							.process(v -> v <= 0 ? bundle.get("newcontrols.unit.nolimit") : Math.round(v / 8) + " " + bundle.get("unit.blocks"))).growX().row();
							
							//mining range
							s.add(new NiceSlider("@newcontrols.ai.prefs.mine-radius", 0, 10, 4, radius -> {
								ai.mineRadius = radius;
							})
							.max(() -> Vars.player.unit().type == null ? 100 : Vars.player.unit().type.miningRange)
							.process(v -> Math.round(v / 8) + " " + bundle.get("unit.blocks"))).growX().row();
							
							//Items selection
							s.add((Element) new Spinner("@newcontrols.ai.prefs.mine-items", items -> {
								items.center().top();
								
								Item.getAllOres().each(i -> {
									final Item item = i; //else it cannot be used in lambdas
									boolean shouldMine = UnitTypes.gamma.mineItems.contains(i);
									
									if (!shouldMine) ai.mineExclude.add(i);
									items.add(new Toggle(i.emoji(), shouldMine, enabled -> {
										if (enabled) {
											ai.mineExclude.remove(item); 
										} else {
											ai.mineExclude.add(item);
										}
									})).size(50);
									if (items.getChildren().size % 6 == 0) items.row();
								});
							})).growX();
							
						})).growX().row();
						
					}, true, () -> !ai.manualMode).growX().row();
					
				}, true, () -> enabled).growX().row();
				
			}).visible(() -> shown).padLeft(8f).row();
			
			//movement joystick
			table.collapser(c -> {
				Joystick move = new Joystick();
				c.add(move).size(200);
				move.used(pos -> {
					ai.moveDir.set(pos);
				});
			}, true, () -> enabled && ai.manualMode);
		});
		
		//aim & shoot joystick
		parent.fill(table -> {
			
			table.center().right();
			
			table.collapser(c -> {
				ActionPanel.buildPortrait(c, ai);
				c.row();
				
				Joystick shoot = new Joystick();
				c.add(shoot).size(200);
				shoot.used(pos -> {
					ai.shootDir.set(pos);
					ai.shoot = true;
				});
			}, true, () -> enabled && ai.manualMode);
			
		});
	}
	
}