package newcontrols.input;

import arc.Core;
import arc.input.KeyCode;
import arc.math.Angles;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.Group;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.ai.Pathfinder;
import mindustry.content.Blocks;
import mindustry.entities.Predict;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.input.InputHandler;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import newcontrols.ui.fragments.ActionPanel;

import static arc.Core.bundle;
import static mindustry.Vars.*;

/** 
 * Emulates basic player actions && handles thumbstick controls, configurable.
 */
public class AIInput extends InputHandler {
	
	public enum AIAction {NONE, AUTO, ATTACK, MINE, PATROL, IDLE};
	
	public Interval updateInterval = new Interval(4);
	public boolean paused = false, manualMode = true;
	public float lastZoom = -1; //no idea
	
	//Whether these actions are enabled. todo: actually make this comprehensible?
	public boolean attack = true, mine = true, patrol = true;
	
	/** Current action selected by the user */
	public AIAction current = AIAction.AUTO;
	/** If the current action is auto, this field is used to save the current auto action */
	public AIAction auto = AIAction.ATTACK;
	
	public Teamc target = null;
	public Tile mineTile = null;
	public Item mineItem = null;
	public boolean mining = false;
	public Tile patrolTile = null;
	
	//resetting
	/** Current movement direction, used for manual control. -1 to 1. Reset every frame. */
	public Vec2 moveDir = new Vec2();
	/** Current shoot direction, used for manual control. -1 to 1. Reset every frame. */
	public Vec2 shootDir = new Vec2();
	/** Whether the unit should shoot, used by manual control. Reset every frame */
	public boolean shoot = false;
	
	//settings
	public float attackRadius = 1200f;
	public float mineRadius = 0f;
	/** Items that won't be mined */
	public Seq<Item> mineExclude = new Seq();
	
	public Unit unitTapped;
	public Building buildingTapped;
	
	public static Vec2 movement = new Vec2();
	
	@Override
	public boolean tap(float x, float y, int count, KeyCode button){
		//if(state.isMenu()) return false;
		
		float worldx = Core.input.mouseWorld(x, y).x, worldy = Core.input.mouseWorld(x, y).y;
		Tile cursor = world.tile(Math.round(worldx / 8), Math.round(worldy / 8));
		
		if (cursor == null || Core.scene.hasMouse(x, y)) return false;
		
		Call.tileTap(player, cursor);
		Tile linked = cursor.build == null ? cursor : cursor.build.tile;
		
		//control units
		if (count == 2) {
			Unit unit = player.unit();
			
			//control a unit/block detected on first tap of double-tap
			if (unitTapped != null) {
				Call.unitControl(player, unitTapped);
				recentRespawnTimer = 1f;
			} else if (buildingTapped != null) {
				Call.buildingControlSelect(player, buildingTapped);
				recentRespawnTimer = 1f;
			} else if(cursor.block() == Blocks.air && unit.within(cursor, unit.type.mineRange)) {
				unit.mineTile = mineTile;
			}
			return false;
		}
		
		tileTappedH(linked.build);
		
		unitTapped = selectedUnit();
		buildingTapped = selectedControlBuild();
		
		return false;
	}
	
	/** @Anuke#4986 why the fuck does this method has default visibility */
	protected boolean tileTappedH(Building build) {
		// !!! notice
		// fully copy-pasted from the superclass
		if(build == null){
			inv.hide();
			config.hideConfig();
			commandBuild = null;
			return false;
		}
		boolean consumed = false, showedInventory = false;

		//select building for commanding
		if(build.block.commandable && commandMode){
			//TODO handled in tap.
			consumed = true;
		}else if(build.block.configurable && build.interactable(player.team())){ //check if tapped block is configurable
			consumed = true;
			if((!config.isShown() && build.shouldShowConfigure(player)) //if the config fragment is hidden, show
				//alternatively, the current selected block can 'agree' to switch config tiles
				|| (config.isShown() && config.getSelected().onConfigureBuildTapped(build))){
				Sounds.click.at(build);
				config.showConfig(build);
			}
			//otherwise...
		}else if(!config.hasConfigMouse()){ //make sure a configuration fragment isn't on the cursor
			//then, if it's shown and the current block 'agrees' to hide, hide it.
			if(config.isShown() && config.getSelected().onConfigureBuildTapped(build)){
				consumed = true;
				config.hideConfig();
			}

			if(config.isShown()){
				consumed = true;
			}
		}

		//call tapped event
		if(!consumed && build.interactable(player.team())){
			build.tapped();
		}

		//consume tap event if necessary
		if(build.interactable(player.team()) && build.block.consumesTap){
			consumed = true;
		}else if(build.interactable(player.team()) && build.block.synthetic() && (!consumed || build.block.allowConfigInventory)){
			if(build.block.hasItems && build.items.total() > 0){
				inv.showFor(build);
				consumed = true;
				showedInventory = true;
			}
		}

		if(!showedInventory){
			inv.hide();
		}

		return consumed;
	}
	
	@Override
	public void buildPlacementUI(Table table){
		table.image().color(Pal.gray).height(4f).colspan(2).growX();
		table.row();
		table.left().margin(0f).defaults().size(48f). left();
		
		
		table.button(b -> b.image(() -> paused ? Icon.pause.getRegion() : Icon.play.getRegion()), Styles.cleari, () -> {
			paused = !paused;
		}).tooltip("@newcontrols.ai.toggle");
		
		table.button(Icon.move, Styles.clearTogglei, () -> {
			manualMode = !manualMode;
		}).update(l -> l.setChecked(manualMode)).tooltip("@ai.manual-mode");
	}
	
	@Override
	public void buildUI(Group origin) {
		super.buildUI(origin);
		origin.fill(t -> {
			t.center().bottom();
			ActionPanel.buildLandscape(t, this);
		});
	}
	
	//REGION CONTROLS
	@Override
	public void update() {
		super.update();
		
		if (!(player.dead() || state.isPaused())) {
			Core.camera.position.lerpDelta(player, Core.settings.getBool("smoothcamera") ? 0.08f : 1f);
		}
		
		if (!ui.chatfrag.shown() && Math.abs(Core.input.axis(Binding.zoom)) > 0) {
			renderer.scaleCamera(Core.input.axis(Binding.zoom));
		}
		
		if (!paused) {
			if (manualMode) {
				manualMovement(player.unit());
				auto = AIAction.NONE;
			} else {
				aiActions(player.unit());
			}
		}
	}
	
	protected void manualMovement(Unit unit) {
		if (!moveDir.isZero()) {
			unit.movePref(moveDir.scl(unit.speed()));
		}
		if (shootDir.isZero()) {
			if (!moveDir.isZero()) {
				aimLook(moveDir.add(unit));
			}
		} else {
			aimLook(shootDir.scl(1600f).add(unit));
		}
		unit.controlWeapons(false, player.shooting = shoot);
		
		//reset to prevent stucking
		moveDir.set(0, 0);
		shootDir.set(0, 0);
		shoot = false;
		unit.mineTile = null;
	}
	
	protected void aiActions(Unit unit) {
		UnitType type = unit.type;
		if (type == null) return;
		
		player.shooting = false;
		unit.mineTile = null;
		
		boolean canAttack = false;
		for (Weapon w : type.weapons) {
			if (w.bullet != null && w.bullet.collides) {
				canAttack = true;
				break;
			}
		}
		
		Building core = unit.closestCore();
		if (core != null && (updateInterval.get(2, 60) || mineItem == null)) {
			mineItem = Item.getAllOres().min(i -> 
				!mineExclude.contains(i) && indexer.hasOre(i) && unit.canMine(i) && core.acceptStack(i, 1, unit) > 0, i -> core.items.get(i)
			);
		}
		
		if (current == AIAction.AUTO && updateInterval.get(20)) {
			if (attack && canAttack && (target = Units.closestTarget(unit.team, unit.x, unit.y, attackRadius > 0 ? attackRadius : Float.MAX_VALUE, t -> true)) != null) {
				auto = AIAction.ATTACK;
			} else if (mine && unit.canMine() && mineItem != null && indexer.findClosestOre(unit, mineItem) != null) {
				auto = AIAction.MINE;
			} else if (patrol && canAttack && ((state.rules.waves && spawner.countSpawns() > 0) || (indexer.getEnemy(unit.team(), BlockFlag.core) != null))) {
				auto = AIAction.PATROL;
			} else {
				auto = AIAction.IDLE;
			}
		}
		
		AIAction action = current != AIAction.AUTO ? current : auto;
		
		switch (action) {
			case ATTACK: attackAI(unit); break;
			case MINE: mineAI(unit); break;
			case PATROL: patrolAI(unit); break;
		}
		
		unit.controlWeapons(false, player.shooting);
	}
	
	protected void attackAI(Unit unit) {
		if (updateInterval.get(1, 10) || Units.invalidateTarget(target, unit.team, unit.x, unit.y)) {
			target = Units.closestTarget(unit.team, unit.x, unit.y, unit.range() * 5, t -> true);
		}
		
		UnitType type = unit.type;
		
		if (target != null && type != null) {
			float bulletSpeed = unit.hasWeapons() ? type.weapons.first().bullet.speed : 0;
			
			float approachRadius = 0.95f;
			float dist = unit.range() * approachRadius;
			float angle = target.angleTo(unit);
			Tmp.v1.set(Angles.trnsx(angle, dist), Angles.trnsy(angle, dist));
			movement.set(target).add(Tmp.v1).sub(unit).limit(unit.speed());
			unit.movePref(movement);
			
			Vec2 intercept = Predict.intercept(unit, target, bulletSpeed);
			player.shooting = unit.within(intercept, unit.range() * 1.25f);
			aimLook(intercept);
		}
	}
	
	//Yes, yes and yes. I literally copied the MinerAI.
	protected void mineAI(Unit unit) {
		Building core = unit.closestCore();
		if(core == null) return;
		
		if (mining) {
			//Core doesn't need this item
			if (mineItem != null && core.acceptStack(mineItem, 1, unit) == 0) {
				if (unit.stack.amount > 0) dropItem(player, unit.rotation);
				mineItem = null;
				return;
			}
			
			//Mine
			if (unit.stack.amount >= unit.type.itemCapacity || (mineItem != null && !unit.acceptsItem(mineItem))) {
				mining = false;
			} else {
				if (updateInterval.get(3, 30) && mineItem != null) {
					mineTile = indexer.findClosestOre(unit, mineItem);
				}
				
				if(mineTile != null){
					movement.set(0, 0).trns(mineTile.angleTo(unit), mineRadius).add(mineTile).sub(unit).limit(unit.speed());
					unit.movePref(movement);
					aimLook(Tmp.v1.set(mineTile).scl(8));
					
					if (mineTile.block() == Blocks.air && unit.within(mineTile, unit.type.mineRange)) {
						unit.mineTile = mineTile;
					}
					
					if (mineTile.block() != Blocks.air) {
						mining = false;
					}
				}
			}
		} else {
			//Unload to core
			if (unit.stack.amount == 0) {
				mining = true;
				return;
			}
			
			if (core.acceptStack(unit.stack.item, unit.stack.amount, unit) > 0) {
				tryDropItems(core, player.x, player.y);
			}
			
			movement.set(core).sub(unit).limit(unit.speed());
			unit.movePref(movement);
			aimLook(core);
		}
	}
	
	protected void patrolAI(Unit unit) {
		Building candidate = Geometry.findClosest(unit.x, unit.y, indexer.getEnemy(unit.team(), BlockFlag.core));
		patrolTile = candidate != null ? candidate.tile() : null;
		
		float offset;
		if (patrolTile != null) {
			offset = unit.range() * 0.8f;
		} else {
			patrolTile = Geometry.findClosest(unit.x, unit.y, Vars.spawner.getSpawns());
			offset = state.rules.dropZoneRadius + 96f;
		}
		
		if (patrolTile != null) {
			if (unit.type.flying) {
				float dst = unit.dst(patrolTile);
				movement.set(patrolTile).sub(unit).limit(unit.speed());
				if (dst < offset - 16f) {
					movement.scl(-1);
					unit.movePref(movement);
				} else if (dst > offset) {
					unit.movePref(movement);
				}
				
				aimLook(patrolTile);
			} else {
				pathfind(unit, Pathfinder.fieldCore);
			}
		}
	}
	//ENDREGION CONTROLS
	
	protected void pathfind(Unit unit, int pathType) {
		int costType = unit.pathType();
		Tile tile = unit.tileOn();
		if (tile == null) return;
		Tile targetTile = pathfinder.getTargetTile(tile, pathfinder.getField(unit.team, costType, pathType));
		
		if (tile == targetTile || (costType == Pathfinder.costNaval && !targetTile.floor().isLiquid)) return;
		
		unit.movePref(movement.set(targetTile).sub(unit).limit(unit.speed()));
		aimLook(targetTile);
	}
	
	@Override
	public boolean zoom(float initialDistance, float distance) {
		//todo: what the fuck does last zoom do
		if (Core.settings.getBool("keyboard")) return false;
		if (lastZoom < 0) {
			lastZoom = renderer.getScale();
		}

		renderer.setScale(distance / initialDistance * lastZoom);
		return true;
	}
	
	/** I have no idea why this method is required. But it just doesn't work on servers if i hardcode these methods. */
	public void tryDropItems(Building build, float x, float y) {
		ItemStack stack = player.unit().stack;
		if (build != null && build.acceptStack(stack.item, stack.amount, player.unit()) > 0 && build.interactable(player.team()) && build.block.hasItems && player.unit().stack().amount > 0 && build.interactable(player.team())) {
			Call.transferInventory(player, build);
		} else {
			Call.dropItem(player.angleTo(x, y));
		}
	}
	
	/** Multiplayer-compatible aiming */
	public void aimLook(Position pos) {
		player.unit().aimLook(pos);
		player.mouseX = pos.getX();
		player.mouseY = pos.getY();
	}
	
	/** Should be called when the AI is being disabled */
	public void finish() {
		player.shooting = false;
		player.unit().mineTile = null;
	}
	
	/** Represents the current AI action, formatted according to bundle */
	@Override
	public String toString() {
		final String first = "newcontrols.ai.action-";
		
		return manualMode ? bundle.get(first + AIAction.NONE) :
		       current == AIAction.AUTO ? bundle.format(first + current, bundle.get(first + auto)) : 
		       bundle.get(first + current);
	}
	
}
