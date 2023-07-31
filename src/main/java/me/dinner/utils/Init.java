package me.dinner.utils;

import me.dinner.utils.commands.AreaSetCommand;
import me.dinner.utils.commands.StashSetCommand;
import me.dinner.utils.hud.ProcessInfoHud;
import me.dinner.utils.modules.Auto6g6sDuper;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class Init {
    public static void init() {
        initCommands();
        initModules();
        initHud();
    }

    private static void initCommands() {
        Commands.add(new AreaSetCommand());
        Commands.add(new StashSetCommand());

    }

    private static void initModules() {
        Modules.get().add(new Auto6g6sDuper());
    }

    private static void initHud() {
        Hud.get().register(ProcessInfoHud.INFO);
    }
}
