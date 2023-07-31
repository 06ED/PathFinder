package me.dinner.utils.modules;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import me.dinner.utils.Addon;
import me.dinner.utils.event.DupeStateChangeEvent;
import me.dinner.utils.utils.DupeProvider;
import me.dinner.utils.utils.DupeUtils;
import me.dinner.utils.utils.State;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Auto6g6sDuper extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAutoSex = settings.createGroup("AutoSex");
    private final SettingGroup sgKill = settings.createGroup("Killing");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> goAway = sgGeneral.add(new BoolSetting.Builder()
        .name("go-away")
        .description("Will be go away from other donkeys to kill target one.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> grownUpChildren = sgGeneral.add(new BoolSetting.Builder()
        .name("grown-up")
        .description("Will be donkeys children grown up by feeding.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> feedCount = sgAutoSex.add(new IntSetting.Builder()
        .name("feed-count")
        .description("How many donkeys will be feed before next step.")
        .defaultValue(2)
        .build()
    );

    private final Setting<Boolean> useSword = sgKill.add(new BoolSetting.Builder()
        .name("use-sword")
        .description("Will be the sword using to kill donkey.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> timeToTakeResources = sgKill.add(new IntSetting.Builder()
        .name("time-to-take-resources")
        .description("How many ticks player will be wait before another actions after killing donkey.")
        .defaultValue(20 * 5)
        .build()
    );


    // Render
    private final Setting<Boolean> renderMA = sgRender.add(new BoolSetting.Builder()
        .name("render-mules-area")
        .description("Is the area with mules will be render.")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> maShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> maSideColor = sgRender.add(new ColorSetting.Builder()
        .name("place-side-color")
        .description("The side color for positions to be placed.")
        .defaultValue(new SettingColor(255, 0, 0, 75))
        .visible(renderMA::get)
        .build()
    );

    private final Setting<SettingColor> maLineColor = sgRender.add(new ColorSetting.Builder()
        .name("place-line-color")
        .description("The line color for positions to be placed.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .visible(renderMA::get)
        .build()
    );

    private int tickTimer = 0;
    private DonkeyEntity targetDonkey;

    private boolean allowBreak;
    private boolean allowPlace;


    public Auto6g6sDuper() {
        super(Addon.CATEGORY, "auto-duper", "Automatically do 6g6s dupe.");
    }

    @Override
    public void onActivate() {
        allowBreak = BaritoneAPI.getSettings().allowBreak.value;
        allowPlace = BaritoneAPI.getSettings().allowPlace.value;

        BaritoneAPI.getSettings().allowBreak.value = false;
        BaritoneAPI.getSettings().allowPlace.value = false;

        super.onActivate();
    }

    @Override
    public void onDeactivate() {
        BaritoneAPI.getSettings().allowBreak.value = allowBreak;
        BaritoneAPI.getSettings().allowPlace.value = allowPlace;

        super.onDeactivate();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.interactionManager == null) return;

        if (targetDonkey == null) {
            Addon.getProvider().setState(State.Multiply);
        } else if (!targetDonkey.isAlive()) {
            targetDonkey = null;
            return;
        }

        switch (Addon.getProvider().getState()) {
            case Multiply -> {
                if (mc.world == null) return;
                int feed = 0;

                for (DonkeyEntity donkey : getDonkeys(6))
                    if (DupeUtils.feedDonkey(donkey)) feed++;

                if (feed >= feedCount.get()) Addon.getProvider().setState(State.GrownUp);
            }
            case GrownUp -> {
                if (!grownUpChildren.get()) {
                    Addon.getProvider().setState(State.Tame);
                    return;
                }

                if (mc.world == null) return;
                int feed = 0;

                for (DonkeyEntity donkey : getDonkeys(6)) {
                    if (!donkey.isBaby()) continue;
                    if (DupeUtils.feedDonkey(donkey)) feed++;
                }

                if (feed >= feedCount.get()) Addon.getProvider().setState(State.Tame);
            }
            case Tame -> {
                if (targetDonkey == null)
                    for (DonkeyEntity donkey : getDonkeys(6))
                        if (donkey.isTame()) targetDonkey = donkey;

                if (targetDonkey != null && targetDonkey.isTame()) {
                    Addon.getProvider().setState(State.Kill);
                    return;
                }

                targetDonkey = getDonkeys(6).get(0);

                tryToTame(targetDonkey);
            }
            case Kill -> {
                if (targetDonkey.isDead()) {
                    targetDonkey = null;

                    if (tickTimer == timeToTakeResources.get()) {
                        tickTimer = 0;
                        Addon.getProvider().setState(State.SortResources);
                        return;
                    }
                    tickTimer++;

                    return;
                }

                if (useSword.get() && targetDonkey.getHealth() >= 17) {
                    FindItemResult result = InvUtils.findInHotbar((itemStack -> itemStack.getItem() instanceof SwordItem));

                    if (result.found()) {
                        mc.player.setSneaking(true);
                        InvUtils.swap(result.slot(), false);
                        mc.interactionManager.attackEntity(mc.player, targetDonkey);
                        mc.player.setSneaking(false);
                    }

                    return;
                }

                if (targetDonkey.isOnFire()) return;

                // Burning up donkey
                FindItemResult result = InvUtils.findInHotbar(Items.FLINT_AND_STEEL);
                if (!result.found()) return;

                BlockHitResult blockHitResult = new BlockHitResult(
                    targetDonkey.getPos(),
                    BlockUtils.getPlaceSide(targetDonkey.getBlockPos()),
                    targetDonkey.getBlockPos(),
                    false
                );
                mc.interactionManager.interactBlock(
                    mc.player,
                    result.getHand(),
                    blockHitResult
                );
                mc.interactionManager.interactBlock(
                    mc.player,
                    result.getHand(),
                    blockHitResult
                );
            }
            case SortResources -> {
                DupeProvider provider = Addon.getProvider();

                BaritoneAPI
                    .getProvider()
                    .getPrimaryBaritone()
                    .getCustomGoalProcess()
                    .setGoalAndPath(
                        new GoalBlock(
                            provider.getStashPos(1).getX() + provider.getStashPos(1).getX() / provider.getStashPos(2).getX(),
                            provider.getStashPos(1).getY(),
                            provider.getStashPos(1).getZ() + provider.getStashPos(1).getZ() / provider.getStashPos(2).getZ()
                        )
                    );

                if (null == mc.world) return;

                WorldChunk chunk = mc.world.getWorldChunk(mc.player.getBlockPos());
                for (Map.Entry<BlockPos, BlockEntity> blockEntry : chunk.getBlockEntities().entrySet()) {
                    if (blockEntry.getValue() instanceof ChestBlockEntity && checkInStash(blockEntry.getKey())) {
                        mc.interactionManager.interactBlock(
                            mc.player,
                            Hand.OFF_HAND,
                            new BlockHitResult(blockEntry.getKey().toCenterPos(), BlockUtils.getPlaceSide(blockEntry.getKey()), blockEntry.getKey(), true)
                        );
                        List<Integer> shulkerList = new ArrayList<>();
                        PlayerInventory inventory = mc.player.getInventory();

                        for (int i = 0; i < inventory.size(); i++) {
                            if (inventory.getStack(i).getItem() == Items.SHULKER_BOX) {
                                if (shulkerList.size() == 15) continue;

                                shulkerList.add(i);
                            }
                        }

                        for (int index : shulkerList) {
                            inventory.removeStack(index);
                        }

                    }
                }

                Addon.getProvider().setState(State.Multiply);
            }
        }
    }

    @EventHandler
    private void onChestOpened(@NotNull OpenScreenEvent event) {
        if (event.screen instanceof GenericContainerScreen) {

        }
    }

    @EventHandler
    private void onChangeDupeState(@NotNull DupeStateChangeEvent event) {
    }

    private boolean checkInStash(@NotNull BlockPos pos) {
        final DupeProvider provider = Addon.getProvider();

        return (provider.getStashPos(1).getX() <= pos.getX() && pos.getX() <= provider.getStashPos(2).getX())
            && (provider.getStashPos(1).getY() <= pos.getY() && pos.getY() <= provider.getStashPos(2).getY())
            && (provider.getStashPos(1).getZ() <= pos.getZ() && pos.getZ() <= provider.getStashPos(2).getZ());
    }

    private void tryToTame(DonkeyEntity entity) {
        if (targetDonkey.hasPlayerRider() || mc.interactionManager == null) return;

        FindItemResult noEatableItem = InvUtils.findInHotbar(itemStack -> !itemStack.isFood());

        if (!noEatableItem.found()) return;

        mc.interactionManager.interactEntity(mc.player, entity, noEatableItem.getHand());
    }

    private @NotNull List<DonkeyEntity> getDonkeys(int distance) {
        if (mc.world == null || mc.player == null) return new ArrayList<>();

        List<DonkeyEntity> donkeyList = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            int xDist = mc.player.getBlockX() - entity.getBlockX();
            int zDist = mc.player.getBlockZ() - entity.getBlockZ();
            int entityDist = (int) Math.sqrt(Math.abs(xDist) + Math.abs(zDist));

            if (entity instanceof DonkeyEntity && entityDist <= distance)
                donkeyList.add((DonkeyEntity) entity);
        }

        return donkeyList;
    }

    @EventHandler
    private void onRender(@NotNull Render3DEvent event) {
        if (renderMA.get())
            event.renderer.box(
                new Box(Addon.getProvider().getPos(1), Addon.getProvider().getPos(2)),
                maSideColor.get(), maLineColor.get(), maShapeMode.get(), 1
            );
    }
}
