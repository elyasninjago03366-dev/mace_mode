package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class ExampleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null || client.interactionManager == null) return;

			// ۱. بررسی دست گرفتن Mace
			boolean isHoldingMace = client.player.getMainHandStack().isOf(Items.MACE);

			// ۲. بررسی حالت سقوط
			boolean isFalling = !client.player.isOnGround() && client.player.getVelocity().y < 0;

			if (!isHoldingMace || !isFalling) return;

			// ۳. بررسی هدف‌گیری و فاصله زیر ۳ بلاک
			HitResult hitResult = client.crosshairTarget;
			if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
				EntityHitResult entityHit = (EntityHitResult) hitResult;

				if (entityHit.getEntity() instanceof PlayerEntity targetPlayer) {
					double distance = client.player.distanceTo(targetPlayer);

					if (distance <= 3.0) {
						if (client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
							client.interactionManager.attack(client.player, targetPlayer);
							client.player.swingHand(Hand.MAIN_HAND);
						}
					}
				}
			}
		});
	}
}
