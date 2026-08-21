package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
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

			// ۲. بررسی حالت سقوط (در حال کم شدن ارتفاع)
			boolean isFalling = !client.player.onGround() && client.player.getDeltaMovement().y < 0;

			if (!isHoldingMace || !isFalling) return;

			// ۳. بررسی نشانه روی هر انتیتی (ماب، پلیر و...) و فاصله زیر ۳ بلاک
			HitResult hitResult = client.hitResult;
			if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
				EntityHitResult entityHit = (EntityHitResult) hitResult;

				if (entityHit.getEntity() instanceof LivingEntity targetEntity) {
					double distance = client.player.distanceTo(targetEntity);

					if (distance <= 3.0) {
						// ضربه آنی به ماب یا پلیر
						client.gameMode.attack(client.player, targetEntity);
						client.player.swing(InteractionHand.MAIN_HAND);
					}
				}
			}
		});
	}
}
