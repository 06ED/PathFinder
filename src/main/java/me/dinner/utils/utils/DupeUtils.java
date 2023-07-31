package me.dinner.utils.utils;

import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class DupeUtils {
    public static boolean feedDonkey(DonkeyEntity donkey) {
        return feedDonkey(donkey, List.of(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.WHEAT, Items.GOLDEN_CARROT, Items.APPLE, Items.SUGAR, Items.HAY_BLOCK));
    }

    public static boolean feedDonkey(DonkeyEntity donkey, List<Item> itemList) {
        return feedDonkey(donkey, itemList, false);
    }

    public static boolean feedDonkey(DonkeyEntity donkey, List<Item> itemList, boolean rotate) {
        if (mc.player == null || mc.interactionManager == null) return false;
        if (!donkey.isAlive()) return false;

        FindItemResult result = InvUtils.findInHotbar(itemStack -> itemList.contains(itemStack.getItem()));
        if (!result.found()) return false;

        final int currentSlot = mc.player.getInventory().selectedSlot;
        InvUtils.swap(result.slot(), false);

        final ActionResult[] actionResult = {null};

        if (rotate)
            Rotations.rotate(Rotations.getYaw(donkey), Rotations.getPitch(donkey), 60, () -> actionResult[0] = mc.interactionManager.interactEntity(
                mc.player,
                donkey,
                result.getHand()
            ));
        else
            actionResult[0] = mc.interactionManager.interactEntity(
                mc.player,
                donkey,
                result.getHand()
            );

        InvUtils.swap(currentSlot, false);

        return actionResult[0].isAccepted();
    }
}
