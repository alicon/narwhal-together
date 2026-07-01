package dev.alicon.copsrobbers;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client-to-server request to toggle creative cruiser flight. */
public record ToggleCruiserFlightPayload() implements CustomPacketPayload {
	/** Stateless singleton payload instance. */
	public static final ToggleCruiserFlightPayload INSTANCE = new ToggleCruiserFlightPayload();
	/** Packet type identifier used by Fabric networking. */
	public static final Type<ToggleCruiserFlightPayload> TYPE =
			new Type<>(CopsAndRobbers.id("toggle_flight"));
	/** Codec for the stateless flight toggle request payload. */
	public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCruiserFlightPayload> CODEC =
			StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
