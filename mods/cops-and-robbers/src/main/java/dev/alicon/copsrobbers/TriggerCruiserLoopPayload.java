package dev.alicon.copsrobbers;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client-to-server request to start a driven cruiser's loop animation. */
public record TriggerCruiserLoopPayload() implements CustomPacketPayload {
	/** Stateless singleton payload instance. */
	public static final TriggerCruiserLoopPayload INSTANCE = new TriggerCruiserLoopPayload();
	/** Packet type identifier used by Fabric networking. */
	public static final Type<TriggerCruiserLoopPayload> TYPE =
			new Type<>(CopsAndRobbers.id("trigger_loop"));
	/** Codec for the stateless loop request payload. */
	public static final StreamCodec<RegistryFriendlyByteBuf, TriggerCruiserLoopPayload> CODEC =
			StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
