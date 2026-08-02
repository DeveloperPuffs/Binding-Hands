package plasmapuffs.bindinghands.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryMixin {
	@Inject(method = "setSelectedSlot", at = @At("HEAD"), cancellable = true)
	private void setSelectedSlot(final int selected, CallbackInfo callbackInformation) {
		Inventory inventory = (Inventory)(Object)this;
		Player player = inventory.player;
		if (player.isCreative()) {
			return;
		}

		if (EnchantmentHelper.has(player.getItemInHand(InteractionHand.MAIN_HAND), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
			callbackInformation.cancel();
		}
	}
}
