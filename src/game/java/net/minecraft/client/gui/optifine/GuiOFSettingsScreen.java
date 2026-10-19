package net.minecraft.client.gui.optifine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

public abstract class GuiOFSettingsScreen extends GuiScreen {

	protected static class Entry {
		final String key;
		final boolean toggle;
		final String[] values;
		final int def;
		final GameSettings.Options option;

		Entry(String key, boolean toggle, int def, String... values) {
			this.key = key;
			this.toggle = toggle;
			this.def = def;
			this.values = values;
			this.option = null;
		}

		Entry(GameSettings.Options option) {
			this.key = null;
			this.toggle = false;
			this.def = 0;
			this.values = null;
			this.option = option;
		}
	}

	private static final int STUB_ID_BASE = 1000;
	private static final Map<String, Integer> STATE = new HashMap<String, Integer>();

	protected final GuiScreen parent;
	protected final String titleKey;
	protected final List<Entry> entries = new ArrayList<Entry>();
	private final List<GuiButton> entryButtons = new ArrayList<GuiButton>();
	private final Map<GuiButton, GameSettings.Options> optionOf = new HashMap<GuiButton, GameSettings.Options>();
	private final Map<GameSettings.Options, GuiButton> buttonOf = new HashMap<GameSettings.Options, GuiButton>();

	protected GuiOFSettingsScreen(GuiScreen parent, String titleKey) {
		this.parent = parent;
		this.titleKey = titleKey;
	}

	protected void toggle(String key, boolean defOn) {
		entries.add(new Entry(key, true, defOn ? 1 : 0, "options.off", "options.on"));
	}

	protected void cycle(String key, int def, String... values) {
		entries.add(new Entry(key, false, def, values));
	}

	protected void option(GameSettings.Options option) {
		entries.add(new Entry(option));
	}

	protected GuiButton buttonFor(GameSettings.Options option) {
		return buttonOf.get(option);
	}

	protected abstract void buildEntries();

	protected void afterInit() {
	}

	protected void onAllButton(boolean on) {
	}

	protected boolean hasAllButtons() {
		return false;
	}

	private static int getValue(Entry e) {
		Integer v = STATE.get(e.key);
		return v == null ? e.def : v.intValue();
	}

	private String text(Entry e) {
		String value = e.values[getValue(e)];
		return I18n.format(e.key, new Object[0]) + ": " + (e.toggle ? I18n.format(value, new Object[0]) : value);
	}

	public void initGui() {
		entries.clear();
		entryButtons.clear();
		optionOf.clear();
		buttonOf.clear();
		buildEntries();
		this.buttonList.clear();
		int y0 = this.height / 6 - 12;
		for (int i = 0; i < entries.size(); ++i) {
			Entry e = entries.get(i);
			int x = this.width / 2 - 155 + (i % 2) * 160;
			int y = y0 + 21 * (i / 2);
			GuiButton btn;
			if (e.option != null) {
				int id = e.option.returnEnumOrdinal();
				btn = e.option.getEnumFloat() ? new GuiOptionSlider(id, x, y, e.option)
						: new GuiOptionButton(id, x, y, e.option, this.mc.gameSettings.getKeyBinding(e.option));
				optionOf.put(btn, e.option);
				buttonOf.put(e.option, btn);
			} else {
				btn = new GuiButton(STUB_ID_BASE + i, x, y, 150, 20, text(e));
			}
			entryButtons.add(btn);
			this.buttonList.add(btn);
		}
		int rows = (entries.size() + 1) / 2;
		int doneY = Math.min(this.height - 27, y0 + 21 * rows + 8);
		String done = I18n.format("gui.done", new Object[0]);
		if (hasAllButtons()) {
			this.buttonList.add(new GuiButton(300, this.width / 2 - 155, doneY, 70, 20,
					I18n.format("of.animation.allOn", new Object[0])));
			this.buttonList.add(new GuiButton(301, this.width / 2 - 82, doneY, 70, 20,
					I18n.format("of.animation.allOff", new Object[0])));
			this.buttonList.add(new GuiButton(200, this.width / 2 - 5, doneY, 160, 20, done));
		} else {
			this.buttonList.add(new GuiButton(200, this.width / 2 - 100, doneY, done));
		}
		afterInit();
	}

	protected void actionPerformed(GuiButton btn) {
		if (!btn.enabled) {
			return;
		}
		if (btn.id == 200) {
			this.mc.gameSettings.saveOptions();
			this.mc.displayGuiScreen(parent);
		} else if (btn.id == 300 || btn.id == 301) {
			for (int i = 0; i < entries.size(); ++i) {
				Entry e = entries.get(i);
				if (e.option == null && e.toggle) {
					STATE.put(e.key, btn.id == 300 ? 1 : 0);
					entryButtons.get(i).displayString = text(e);
				}
			}
			onAllButton(btn.id == 300);
			for (int i = 0; i < entries.size(); ++i) {
				Entry e = entries.get(i);
				if (e.option != null && !e.option.getEnumFloat()) {
					entryButtons.get(i).displayString = this.mc.gameSettings.getKeyBinding(e.option);
				}
			}
		} else if (optionOf.containsKey(btn)) {
			GameSettings.Options o = optionOf.get(btn);
			if (btn instanceof GuiOptionButton) {
				this.mc.gameSettings.setOptionValue(o, 1);
				btn.displayString = this.mc.gameSettings.getKeyBinding(o);
			}
		} else if (btn.id >= STUB_ID_BASE && btn.id - STUB_ID_BASE < entries.size()) {
			Entry e = entries.get(btn.id - STUB_ID_BASE);
			STATE.put(e.key, (getValue(e) + 1) % e.values.length);
			btn.displayString = text(e);
		}
	}

	public void drawScreen(int i, int j, float f) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, I18n.format(titleKey, new Object[0]), this.width / 2, 15,
				16777215);
		super.drawScreen(i, j, f);
	}
}