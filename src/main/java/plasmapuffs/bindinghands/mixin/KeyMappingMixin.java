package plasmapuffs.bindinghands.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void consumeClick(CallbackInfoReturnable<Boolean> callbackInformationReturnable) {
        KeyMapping keyMapping = (KeyMapping)(Object)this;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer == null || localPlayer.isCreative()) {
            return;
        }

        if (keyMapping == minecraft.options.keyDrop) {
            if (EnchantmentHelper.has(localPlayer.getItemInHand(InteractionHand.MAIN_HAND), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                callbackInformationReturnable.setReturnValue(false);
                callbackInformationReturnable.cancel();
            }
        } else if (keyMapping == minecraft.options.keySwapOffhand) {
            if (
                    EnchantmentHelper.has(localPlayer.getItemInHand(InteractionHand.MAIN_HAND), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) ||
                    EnchantmentHelper.has(localPlayer.getItemInHand(InteractionHand.OFF_HAND), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
            ) {
                callbackInformationReturnable.setReturnValue(false);
                callbackInformationReturnable.cancel();
            }
        }
    }
}
