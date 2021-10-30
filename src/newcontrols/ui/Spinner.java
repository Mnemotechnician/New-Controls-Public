package newcontrols.ui;

import arc.*;
import arc.func.*;
import arc.struct.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.utils.*;
import mindustry.gen.*;
import mindustry.ui.*;

/** Text button, clicking on which shows/hides a collapser. The collapser is displayed over other elements. */
public class Spinner extends TextButton {
	
	public Collapser col;
	public TextButton button;
	public Image image;
	
	/** Whether to remove collapser if any of ancestors are invisible / untouchable */
	public boolean autoHide = true;
	protected float collapseTime = 0.4f;
	protected float padW = 16f, padH = 16f; //padding cus else it looks ugly
	
	Vec2 tmp = new Vec2();
	Interval hideInterval = new Interval();
	Timer.Task hideTask;
	
	public Spinner(String header, Cons<Table> constructor) {
		super(header, Styles.clearTogglet);
		add(image = new Image(Icon.downOpen)).size(Icon.downOpen.imageSize() * Scl.scl(1f)).padLeft(padW / 2f).left();
		getCells().reverse();
		
		clicked(() -> {
			col.toggle();
			if (col.isCollapsed()) {
				hide(true);
			} else {
				show(true);
			}
		});
		
		update(() -> {
			setChecked(!col.isCollapsed());
			image.setDrawable(!col.isCollapsed() ? Icon.upOpen : Icon.downOpen);
		});
		
		col = new Collapser(base -> base.pane(t -> {
			t.left();
			constructor.get(t);
		}).growX().scrollX(false), true).setDuration(collapseTime);
		
		col.update(() -> {
			if (col.getScene() != null) {
				col.visible = true;
				col.color.a = parentAlpha * color.a;
				col.setSize(width, col.getPrefHeight());
				
				Vec2 point = localToStageCoordinates(tmp.set(0, -col.getPrefHeight()));
				if (point.y < getPrefHeight()) point = localToStageCoordinates(tmp.set(0, col.getPrefHeight()));
				col.setPosition(point.x, point.y);
			}
			
			if (autoHide && col.getScene() != null && hideInterval.get(8)) {
				//find any invisible or not touchable ancestors, hide if found
				Element current = this;
				while (true) {
					if (!current.visible || current.touchable == Touchable.disabled) {
						hide(false);
						break;
					}
					if (current.parent == null) break;
					current = current.parent;
				}
			}
		});
	}
	
	public void show(boolean animate) {
		if (hideTask != null) hideTask.cancel();
		
		col.setCollapsed(false, animate);
		
		Scene prevStage = col.getScene();
		if (prevStage != null) prevStage.root.removeChild(col);
		Scene stage = getScene();
		if (stage == null) return;
		stage.add(col);
		
		col.toFront();
		toFront();
	}
	
	public void hide(boolean animate) {
		col.setCollapsed(true, animate);
		Scene stage = getScene();
		
		if (stage != null) {
			if (animate) {
				hideTask = Timer.schedule(() -> {stage.root.removeChild(col); hideTask = null;}, collapseTime);
			} else {
				stage.root.removeChild(col);
			}
		}
	}
	
	@Override
	public float getPrefWidth() {
		return super.getPrefWidth() + padW;
	}
	
	@Override
	public float getPrefHeight() {
		return super.getPrefWidth() + padH;
	}
	
}