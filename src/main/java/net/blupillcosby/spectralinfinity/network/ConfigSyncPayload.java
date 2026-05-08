package net.blupillcosby.spectralinfinity.network;

import net.blupillcosby.spectralinfinity.SpectralInfinity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigSyncPayload(String configJson) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.parse(SpectralInfinity.MOD_ID + ":config_sync");
    public static final Type<ConfigSyncPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC = CustomPacketPayload.codec(
            ConfigSyncPayload::write, ConfigSyncPayload::new
    );

    private ConfigSyncPayload(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.configJson);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
