package plasmapuffs.bindinghands.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
    @SuppressWarnings("resource")
    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void doClick(int slotIndex, int buttonNum, ContainerInput containerInput, Player player, CallbackInfo callbackInformation) {
        if (player.isCreative()) {
            return;
        }

        AbstractContainerMenu abstractContainerMenu = (AbstractContainerMenu)(Object)this;
        if (slotIndex < 0 || slotIndex >= abstractContainerMenu.slots.size()) {
            return;
        }

        Slot clickedSlot = abstractContainerMenu.slots.get(slotIndex);
        if (!(clickedSlot.container instanceof Inventory inventory)) {
            return;
        }

        // TODO: Fix potential bug that causes another identical piece of armor present in the inventory to be equipped when shift-double-clicking the held cursed armor piece on the hotbar
        // I haven't figured out why it happens nor how to reproduce it

        int clickedSlotContainerIndex = clickedSlot.getContainerSlot();
        if (EnchantmentHelper.has(inventory.getItem(clickedSlotContainerIndex), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            if (clickedSlotContainerIndex == inventory.getSelectedSlot() || clickedSlotContainerIndex == Inventory.SLOT_OFFHAND) {
                callbackInformation.cancel();

                if (!player.level().isClientSide()) {
                    ServerPlayer serverPlayer = (ServerPlayer)player;
                    serverPlayer.inventoryMenu.broadcastFullState();
                }
            }
        }
    }
}
