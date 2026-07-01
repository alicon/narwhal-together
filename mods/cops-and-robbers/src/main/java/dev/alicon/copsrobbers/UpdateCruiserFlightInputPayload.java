package dev.alicon.copsrobbers;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client-to-server flight lift input while driving the cruiser. */
public record UpdateCruiserFlightInputPayload(float lift) implements CustomPacketPayload {
	/** Packet type identifier used by Fabric networking. */
	public static final Type<UpdateCruiserFlightInputPayload> TYPE =
			new Type<>(CopsAndRobbers.id("update_flight_input"));
	/** Codec for the current vertical flight input. */
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateCruiserFlightInputPayload> CODEC =
			StreamCodec.of(
					(buffer, payload) -> buffer.writeFloat(payload.lift()),
					buffer -> new UpdateCruiserFlightInputPayload(buffer.readFloat())
			);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
