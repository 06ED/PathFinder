package me.dinner.utils.hud;

import me.dinner.utils.Addon;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class ProcessInfoHud extends HudElement {
    public static final HudElementInfo<ProcessInfoHud> INFO = new HudElementInfo<>(Addon.HUD_GROUP, "State", "HUD element for display dupe state.", ProcessInfoHud::new);

    public ProcessInfoHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (Addon.getProvider().getState() == null) return;

        final String text = Addon.getProvider().getState().name();

        setSize(renderer.textWidth(text, true), renderer.textHeight(true));
        renderer.text(text, x, y, Color.WHITE, true);
        renderer.quad(x, y, getWidth(), getHeight(), new Color(155, 0, 25, 255));
    }
}
