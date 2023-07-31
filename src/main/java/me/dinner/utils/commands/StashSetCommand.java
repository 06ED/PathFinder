package me.dinner.utils.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.dinner.utils.Addon;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

public class StashSetCommand extends Command {
    public StashSetCommand() {
        super("stash", "Set your stash area");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("pos", IntegerArgumentType.integer()).executes(context -> {
                final int pos = context.getArgument("pos", Integer.class);

                if (pos != 1 && pos != 2) error("You can set this parameter only 1 or 2.");
                if (Addon.getProvider() == null) error("No dupe provider found, try again.");
                if (mc.player == null) error("No player found.");

                Addon.getProvider().setStashPos(pos, mc.player.getBlockPos().add(0, -1, 0));

                return SINGLE_SUCCESS;
            })
        );
    }
}
