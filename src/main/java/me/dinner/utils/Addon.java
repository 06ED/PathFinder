package me.dinner.utils;

import com.mojang.logging.LogUtils;
import me.dinner.utils.utils.DupeProvider;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class Addon extends MeteorAddon {
    public static final HudGroup HUD_GROUP = new HudGroup("6G6SUtils");
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("6G6SUtils");

    private static DupeProvider provider;

    @Override
    public void onInitialize() {
        LOG.info("Initializing 6G6SUtils Meteor addon...");

        Init.init();

        provider = new DupeProvider();
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "me.dinner.utils";
    }

    public static DupeProvider getProvider() {
        return provider;
    }
}
