package com.anonymoushavoc.relic;

import lombok.Getter;

@Getter
public enum Relic {
    HUNTERS_COMPASS("Hunter's Compass", 1001, 10, 1),
    SIGHT_GLASS("Sight-Glass", 1002, 8, 4),
    BELL_OF_TRUTH("Bell of Truth", 1003, 4, 7),
    TOTEM_OF_SCRAMBLING("Totem of Scrambling", 1004, 5, 10),
    SHARD_OF_ANONYMITY("Shard of Anonymity", 1005, 2, 13),
    SOUL_BINDER_DAGGER("Soul-Binder Dagger", 1006, 1, 16);
    
    private final String displayName;
    private final int customModelData;
    private final int globalLimit;
    private final int releaseDay;
    
    Relic(String displayName, int customModelData, int globalLimit, int releaseDay) {
        this.displayName = displayName;
        this.customModelData = customModelData;
        this.globalLimit = globalLimit;
        this.releaseDay = releaseDay;
    }
    
    public static Relic fromCustomModelData(int cmd) {
        for (Relic relic : values()) {
            if (relic.customModelData == cmd) {
                return relic;
            }
        }
        return null;
    }
}