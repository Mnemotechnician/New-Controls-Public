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

import newcontrols.func.*;

/** Slider with extra funny stuff */
public class NiceSlider extends Table {
	
	StringProcessor processor;
	Provf minProv, maxProv;
	
	Slider slider;
	Label title, amount;
	
	public NiceSlider(String title, float min, float max, float step, float def, boolean vertical, Floatc cons) {
		slider = new Slider(min, max, def, vertical);
		slider.moved(cons);
		
		Table content = new Table();
		content.margin(3f, 33f, 3f, 33f);
		content.touchable = Touchable.disabled;
		content.add(this.title = new Label(title, Styles.outlineLabel)).left().growX();
		content.add(this.amount = new Label("", Styles.outlineLabel)).right().padLeft(10f);
		
		stack(slider, content).left().growX();
	}
	
	public NiceSlider(String title, float min, float max, float def, Floatc cons) {
		this(title, min, max, 1, def, false, cons);
	}
	
	public NiceSlider(String title, float min, float max, Floatc cons) {
		this(title, min, max, 1, 0, false, cons);
	}
	
	public NiceSlider process(StringProcessor processor) {
		this.processor = processor;
		return this;
	}
	
	public NiceSlider min(Provf prov) {
		this.minProv = prov;
		return this;
	}
	
	public NiceSlider max(Provf prov) {
		this.maxProv = prov;
		return this;
	}
	
	@Override
	public void act(float delta) {
		super.act(delta);
		if (processor != null) amount.setText(processor.get(slider.getValue()));
		
		if (minProv != null || maxProv != null) {
			//todo: is this shit required?
			float min = minProv != null ? minProv.get() : slider.getMinValue();
			float max = maxProv != null ? maxProv.get() : slider.getMaxValue();
			slider.setRange(min, max);
		}
	}

}