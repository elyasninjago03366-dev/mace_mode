package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ExampleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null || client.gameMode == null) return;

			// ۱. بررسی دست گرفتن Mace
			boolean isHoldingMace = client.player.getMainHandItem().is(Items.MACE);

			// ۲. بررسی حالت سقوط
			boolean isFalling = !client.player.onGround() && client.player.getDeltaMovement().y < 0;

			if (!isHoldingMace || !isFalling) return;

			// ۳. بررسی هدف‌گیری و فاصله ریچ زیر ۳ بلاک
			HitResult hitResult = client.hitResult;
			if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
				EntityHitResult entityHit = (EntityHitResult) hitResult;

				if (entityHit.getEntity() instanceof Player targetPlayer) {
					double distance = client.player.distanceTo(targetPlayer);

					if (distance <= 3.0) {
						if (client.player.getAttackStrengthScale(0.5f) >= 1.0f) {
							client.gameMode.attack(client.player, targetPlayer);
							client.player.swing(InteractionHand.MAIN_HAND);
						}
					}
				}
			}
		});
	}
}
