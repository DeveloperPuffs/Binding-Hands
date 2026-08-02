package plasmapuffs.bindinghands.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Equippable.class)
public class EquippableMixin {
    @SuppressWarnings("resource")
    @Inject(method = "swapWithEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private void swapWithEquipmentSlot(final ItemStack inHand, final Player player, CallbackInfoReturnable<InteractionResult> callbackInformationReturnable) {
        if (player.isCreative()) {
            return;
        }

        // Make sure the player is currently allowed to equip that type of equipment
        Equippable equippable = (Equippable)(Object)this;
        if (!(player.canUseSlot(equippable.slot()) && equippable.canBeEquippedBy(player.typeHolder()))) {
            return;
        }

        // There's no need to check if the currently equipped piece is enchanted with curse of binding because Minecraft already prevents it from being taken off
        if (EnchantmentHelper.has(inHand, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            callbackInformationReturnable.setReturnValue(InteractionResult.FAIL);
            callbackInformationReturnable.cancel();

            if (!player.level().isClientSide()) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                serverPlayer.inventoryMenu.broadcastFullState();
            }
        }
    }
}
