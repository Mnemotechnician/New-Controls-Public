package newcontrols.ui;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.*;

public class Joystick extends Element {
	public Color backColor = new Color(0, 0, 0), stickColor = new Color(0.7f, 0.7f, 0.7f);

	public Cons<Vec2> usedListener;
	public Vec2 offset = new Vec2();
	public boolean isDragging = false;

	private static final Vec2 tmp = new Vec2();
	private static final Vec2 tmp2 = new Vec2();

	public Joystick() {
		addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
				isDragging = true;
				touchDragged(event, x, y, pointer); //set offset
				return true;
			}

			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				float radius = Math.min(width, height) / 2f;
				offset.set(x - width / 2, y - width / 2).limit(radius);
			}

			@Override
			public void touchUp(InputEvent e, float x, float y, int pointer, KeyCode button) {
				isDragging = false;
				offset.set(0, 0);
				if (usedListener != null) usedListener.get(getMovement());
			}
		});
	}

	/**
	 * Called when the joystick is used. vec2 has a length length from -1 to 1
	 */
	public void used(Cons<Vec2> usedListener) {
		this.usedListener = usedListener;
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		if (isDragging && usedListener != null) usedListener.get(getMovement());
	}

	@Override
	public void draw() {
		super.draw();
		float radius = Math.min(width, height) / 2f;

		float x = this.x + width / 2;
		float y = this.y + height / 2;

		tmp.set(offset).limit(radius * 0.8f); //this way the stick will always be in le circle

		Draw.color(backColor);
		Draw.alpha(0.5f);
		Fill.circle(x, y, radius);
		Fill.circle(x, y, radius / 1.5f);

		Draw.color(stickColor);
		Fill.circle(x + tmp.x, y + tmp.y, radius / 5f);
	}

	/**
	 * Movement vector with a range of [-1; 1]
	 */
	public Vec2 getMovement() {
		return tmp.set(offset).div(tmp2.set(width / 2, height / 2));
	}
}
