package com.anonymoushavoc.relic;

import lombok.Data;

import java.util.UUID;

@Data
public class RelicData {
    private Relic type;
    private UUID ownerUuid;
    private long craftedDate;
    private String lastLocationWorld;
    private double lastLocationX;
    private double lastLocationY;
    private double lastLocationZ;
    private UUID lockedMobUuid;
}