package ru.femboypve.hcscr;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DisablingPayload(boolean disabled) implements CustomPacketPayload {
    public static final StreamCodec<ByteBuf, DisablingPayload> STREAM_CODEC = ByteBufCodecs.BOOL.map(DisablingPayload::new, DisablingPayload::disabled);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return HCsCRFabric.LOCATION;
    }
}
