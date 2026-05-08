package net.blupillcosby.spectralinfinity.network;

import net.blupillcosby.spectralinfinity.SpectralInfinity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigSavePayload(String configJson) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.parse(SpectralInfinity.MOD_ID + ":config_save");
    public static final Type<ConfigSavePayload> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ConfigSavePayload> STREAM_CODEC = CustomPacketPayload.codec(
            ConfigSavePayload::write, ConfigSavePayload::new
    );

    private ConfigSavePayload(FriendlyByteBuf buf) {
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
