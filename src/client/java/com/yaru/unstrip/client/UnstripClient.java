package com.yaru.unstrip.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;

public class UnstripClient implements ClientModInitializer {

	private static final KeyMapping.Category CATEGORY =
			KeyMapping.Category.register(
					Identifier.fromNamespaceAndPath("unstrip", "general")
			);

	private static final KeyMapping TOGGLE_KEY =
			KeyMappingHelper.registerKeyMapping(
					new KeyMapping(
							"key.unstrip.toggle",
							InputConstants.Type.KEYSYM,
							InputConstants.KEY_Y,
							CATEGORY
					)
			);

	private static boolean protectionEnabled = true;

	@Override
	public void onInitializeClient() {

		// Register the toggle key.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TOGGLE_KEY.consumeClick()) {

				protectionEnabled = !protectionEnabled;

				if (client.player != null) {
					String status = protectionEnabled ? "ON" : "OFF";

					client.player.sendSystemMessage(
							Component.literal(
									"Unstrip protection: " + status
							)
					);
				}
			}
		});

		// Prevent accidental log stripping.
		UseBlockCallback.EVENT.register(
				(player, level, hand, hitResult) -> {

					if (!protectionEnabled) {
						return InteractionResult.PASS;
					}

					var stack = player.getItemInHand(hand);

					boolean isAxe = stack.getItem() instanceof AxeItem;

					boolean isLog = level.getBlockState(
							hitResult.getBlockPos()
					).is(BlockTags.LOGS);

					if (isAxe && isLog) {
						return InteractionResult.FAIL;
					}

					return InteractionResult.PASS;
				}
		);
	}
}