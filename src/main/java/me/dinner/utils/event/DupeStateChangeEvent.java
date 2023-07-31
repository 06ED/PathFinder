package me.dinner.utils.event;

import me.dinner.utils.utils.State;

public class DupeStateChangeEvent {
    public static final DupeStateChangeEvent INSTANCE = new DupeStateChangeEvent();

    public State state;

    public static DupeStateChangeEvent get(State state) {
        INSTANCE.state = state;

        return INSTANCE;
    }
}
