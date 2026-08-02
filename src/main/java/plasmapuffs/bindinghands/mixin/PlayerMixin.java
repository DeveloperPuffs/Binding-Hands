package plasmapuffs.bindinghands.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @SuppressWarnings("resource")
    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void interactOn(final Entity entity, final InteractionHand hand, final Vec3 location, CallbackInfoReturnable<InteractionResult> callbackInformationReturnable) {
        Player player = (Player)(Object)this;

        // The spectator check is also done in the original Player.interactOn method
        if (player.isSpectator() || player.isCreative()) {
            return;
        }

        if (EnchantmentHelper.has(player.getItemInHand(InteractionHand.MAIN_HAND), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            if (entity instanceof ArmorStand || entity instanceof ItemFrame) {
                callbackInformationReturnable.setReturnValue(InteractionResult.FAIL);
                callbackInformationReturnable.cancel();

                if (!player.level().isClientSide()) {
                    ServerPlayer serverPlayer = (ServerPlayer)player;
                    serverPlayer.inventoryMenu.broadcastFullState();
                }
            }
        }
    }
}
