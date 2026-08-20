package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "modid";

	@Override
	public void onInitialize() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.level == null || client.gameMode == null) return;

			// ۱. بررسی داشتن Mace در دست اصلی
			boolean isHoldingMace = client.player.getMainHandItem().is(Items.MACE);

			// ۲. بررسی حالت سقوط (توی هوا باشه و سرعت حرکت رو به پایین باشه)
			boolean isFalling = !client.player.onGround() && client.player.getDeltaMovement().y < 0;

			if (!isHoldingMace || !isFalling) return;

			// ۳. بررسی هدف‌گیری روی پلیر و فاصله ۳ بلاک یا کمتر
			HitResult hitResult = client.hitResult;
			if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
				EntityHitResult entityHit = (EntityHitResult) hitResult;

				if (entityHit.getEntity() instanceof Player targetPlayer) {
					double distance = client.player.distanceTo(targetPlayer);

					if (distance <= 3.0) {
						// بررسی پر بودن کول‌داون حمله (برای ضربه ۱۰۰٪ و دور زدن آنتی‌چیت)
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
