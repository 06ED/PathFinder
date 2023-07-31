package me.dinner.utils.utils;

import me.dinner.utils.event.DupeStateChangeEvent;
import net.minecraft.util.math.BlockPos;

import static meteordevelopment.meteorclient.MeteorClient.EVENT_BUS;

public class DupeProvider {
    private BlockPos pos1;
    private BlockPos pos2;

    private BlockPos stashPos1;
    private BlockPos stashPos2;

    private State state;

    public void setState(State state) {
        this.state = state;
        EVENT_BUS.post(DupeStateChangeEvent.get(state));
    }

    public State getState() {
        return state;
    }

    public void setPos(int number, BlockPos value) {
        if (number == 1) pos1 = value;
        else if (number == 2) pos2 = value;
    }

    public BlockPos getPos(int number) {
        return number == 1 ? pos1 : pos2;
    }

    public void setStashPos(int number, BlockPos value) {
        if (number == 1) stashPos1 = value;
        else if (number == 2) stashPos2 = value;
    }

    public BlockPos getStashPos(int number) {
        return number == 1 ? stashPos1 : stashPos2;
    }

    public boolean checkInArea(BlockPos pos) {
        return false; // TODO
    }
}
