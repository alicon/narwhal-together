package dev.alicon.copsrobbers;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client-to-server request to toggle a driven cruiser's lightbar. */
public record ToggleCruiserLightsPayload() implements CustomPacketPayload {
	/** Stateless singleton payload instance. */
	public static final ToggleCruiserLightsPayload INSTANCE = new ToggleCruiserLightsPayload();
	/** Packet type identifier used by Fabric networking. */
	public static final Type<ToggleCruiserLightsPayload> TYPE =
			new Type<>(CopsAndRobbers.id("toggle_lights"));
	/** Codec for the stateless light toggle request payload. */
	public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCruiserLightsPayload> CODEC =
			StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
