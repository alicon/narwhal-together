package dev.alicon.copsrobbers;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client-to-server request to toggle a driven cruiser's siren. */
public record ToggleCruiserSirenPayload() implements CustomPacketPayload {
	/** Stateless singleton payload instance. */
	public static final ToggleCruiserSirenPayload INSTANCE = new ToggleCruiserSirenPayload();
	/** Packet type identifier used by Fabric networking. */
	public static final Type<ToggleCruiserSirenPayload> TYPE =
			new Type<>(CopsAndRobbers.id("toggle_siren"));
	/** Codec for the stateless siren toggle request payload. */
	public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCruiserSirenPayload> CODEC =
			StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
