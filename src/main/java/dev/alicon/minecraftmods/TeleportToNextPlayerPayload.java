package dev.alicon.minecraftmods;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TeleportToNextPlayerPayload() implements CustomPacketPayload {
	public static final TeleportToNextPlayerPayload INSTANCE = new TeleportToNextPlayerPayload();
	public static final Type<TeleportToNextPlayerPayload> TYPE =
			new Type<>(MinecraftMods.id("teleport_to_next_player"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TeleportToNextPlayerPayload> CODEC =
			StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
