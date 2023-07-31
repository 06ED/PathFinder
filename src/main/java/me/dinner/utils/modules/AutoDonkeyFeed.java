package me.dinner.utils.modules;

import baritone.api.utils.RotationUtils;
import me.dinner.utils.Addon;
import me.dinner.utils.utils.DupeUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class AutoDonkeyFeed extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAutoDisable = settings.createGroup("AutoDisable");

    private final Setting<Integer> feedRange = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("Distance to feed donkeys.")
        .min(0)
        .max(7)
        .defaultValue(5)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("WIll be rotate player to the donkey.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> feedPeerTick = sgGeneral.add(new IntSetting.Builder()
        .name("feed-peer-tick")
        .description("How many times can feed per tick.")
        .min(1)
        .max(20)
        .defaultValue(5)
        .build()
    );

    private final Setting<List<Item>> allowedFoodList = sgGeneral.add(new ItemListSetting.Builder()
        .name("allowed-food-list")
        .description("What kind of food can be use to feed donkeys.")
        .filter(Item::isFood)
        .defaultValue(List.of(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.WHEAT, Items.GOLDEN_CARROT, Items.APPLE, Items.SUGAR, Items.HAY_BLOCK))
        .build()
    );

    private final Setting<Boolean> grownUp = sgGeneral.add(new BoolSetting.Builder()
        .name("allow-grown-up-babies")
        .description("Will be food use to grown up donkeys' babies.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoDisable = sgAutoDisable.add(new BoolSetting.Builder()
        .name("auto-disable")
        .description("Will be automatically disable module when donkeys will be feed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> feedCount = sgAutoDisable.add(new IntSetting.Builder()
        .name("feed-count")
        .description("How many donkeys will be feed before disable.")
        .visible(autoDisable::get)
        .defaultValue(4)
        .min(1)
        .max(30)
        .build()
    );

    private int feedCounter;
    private List<DonkeyEntity> donkeys = new ArrayList<>();

    public AutoDonkeyFeed() {
        super(Addon.CATEGORY, "AutoDonkeyFeed", "Automatically feeds nearest donkeys.");
    }

    @Override
    public void onActivate() {
        feedCounter = 0;
        retarget();

        super.onActivate();
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOWEST)
    private void onTick(TickEvent.Post event) {
        if (feedCounter == feedCount.get() && autoDisable.get()) {
            toggle();
            return;
        }
        if (donkeys.isEmpty()) {
            retarget();
            return;
        }

        final List<DonkeyEntity> feedDonkeys = new ArrayList<>();
        int tickCounter = 0;

        for (DonkeyEntity donkey : donkeys) {
            if (tickCounter == feedPeerTick.get()) return;
            tickCounter++;

            if (donkey.isBaby() && grownUp.get()) {
                if (DupeUtils.feedDonkey(donkey, allowedFoodList.get(), rotate.get())) feedCounter++;
                if (!donkey.isBaby()) feedDonkeys.add(donkey);

                continue;
            }

            if (DupeUtils.feedDonkey(donkey, allowedFoodList.get(), rotate.get())) {
                feedDonkeys.add(donkey);
                feedCounter++;
            }
        }

        donkeys.removeAll(feedDonkeys);
    }

    private void retarget() {
        if (mc.player == null || mc.world == null) return;

        donkeys = mc.world.getEntitiesByClass(
            DonkeyEntity.class,
            new Box(
                mc.player.getBlockPos().add(feedRange.get(), feedRange.get(), feedRange.get()),
                mc.player.getBlockPos().add(-feedRange.get(), -feedRange.get(), -feedRange.get())
            ),
            (entity) -> entity.canEat() || (entity.isBaby() && grownUp.get())
        );
    }
}
