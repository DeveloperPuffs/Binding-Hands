package plasmapuffs.bindinghands.mixin;

import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Inject(method = "handleSetCarriedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER), cancellable = true)
    private void handleSetCarriedItem(final ServerboundSetCarriedItemPacket packet, CallbackInfo callbackInformation) {
        // The invalid slot check is also done in the original ServerGamePacketListnerImpl.handleSetCarriedItem method
        if (packet.getSlot() >= 0 && packet.getSlot() < Inventory.getSelectionSize()) {
            return;
        }

        ServerGamePacketListenerImpl serverGamePacketListenerImplementation = (ServerGamePacketListenerImpl)(Object)this;
        ServerPlayer serverPlayer = serverGamePacketListenerImplementation.getPlayer();
        if (serverPlayer.isCreative()) {
            return;
        }

        Inventory inventory = serverPlayer.getInventory();
        int selectedSlot = inventory.getSelectedSlot();
        if (EnchantmentHelper.has(inventory.getItem(selectedSlot), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            if (packet.getSlot() != selectedSlot) {
                callbackInformation.cancel();
                serverPlayer.connection.send(new ClientboundSetHeldSlotPacket(selectedSlot));
            }
        }
    }

    @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket;getPos()Lnet/minecraft/core/BlockPos;", shift = At.Shift.AFTER), cancellable = true)
    private void handlePlayerAction(final ServerboundPlayerActionPacket packet, CallbackInfo callbackInformation) {
        ServerGamePacketListenerImpl serverGamePacketListenerImplementation = (ServerGamePacketListenerImpl)(Object)this;
        ServerPlayer serverPlayer = serverGamePacketListenerImplementation.getPlayer();
        if (serverPlayer.isCreative()) {
            return;
        }

        ServerboundPlayerActionPacket.Action action = packet.getAction();
        switch (action) {
            case DROP_ITEM: {
                if (EnchantmentHelper.has(serverPlayer.getItemInHand(InteractionHand.MAIN_HAND), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                    callbackInformation.cancel();
                    serverPlayer.inventoryMenu.broadcastFullState();
                }

                break;
            }

            case SWAP_ITEM_WITH_OFFHAND: {
                if (
                        EnchantmentHelper.has(serverPlayer.getItemInHand(InteractionHand.MAIN_HAND), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) ||
                        EnchantmentHelper.has(serverPlayer.getItemInHand(InteractionHand.OFF_HAND), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
                ) {
                    callbackInformation.cancel();
                    serverPlayer.inventoryMenu.broadcastFullState();
                }

                break;
            }

            default: break;
        }
    }
}
