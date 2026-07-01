package dev.alicon.copsrobbers;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client-to-server request to start a driven cruiser's barrel roll animation. */
public record TriggerCruiserBarrelRollPayload() implements CustomPacketPayload {
	/** Stateless singleton payload instance. */
	public static final TriggerCruiserBarrelRollPayload INSTANCE = new TriggerCruiserBarrelRollPayload();
	/** Packet type identifier used by Fabric networking. */
	public static final Type<TriggerCruiserBarrelRollPayload> TYPE =
			new Type<>(CopsAndRobbers.id("trigger_barrel_roll"));
	/** Codec for the stateless barrel roll request payload. */
	public static final StreamCodec<RegistryFriendlyByteBuf, TriggerCruiserBarrelRollPayload> CODEC =
			StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
