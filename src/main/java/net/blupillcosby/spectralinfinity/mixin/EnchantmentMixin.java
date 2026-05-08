package net.blupillcosby.spectralinfinity.mixin;

import net.blupillcosby.spectralinfinity.config.ModConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @Inject(method = "modifyAmmoCount", at = @At("HEAD"))
    private void spectral_infinity$overrideInfinityAmmo(ServerLevel serverLevel, int enchantmentLevel, ItemStack itemStack, MutableFloat change, CallbackInfo ci) {
        Enchantment enchantment = (Enchantment) (Object) this;
        Identifier id = serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getKey(enchantment);
        
        if (id != null && id.toString().equals("minecraft:infinity")) {
            ModConfig config = ModConfig.get();
            net.minecraft.world.item.Item item = itemStack.getItem();
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            
            if (itemId != null) {
                String arrowKey = itemId.toString();
                
                // Handle tipped arrows uniquely
                if (itemStack.is(net.minecraft.world.item.Items.TIPPED_ARROW)) {
                    net.minecraft.world.item.alchemy.PotionContents contents = itemStack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
                    if (contents != null && contents.potion().isPresent()) {
                        net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion = contents.potion().get();
                        String potionId = potion.unwrapKey().map(key -> key.identifier().toString()).orElse("");
                        if (!potionId.isEmpty()) {
                            arrowKey = arrowKey + "#" + potionId;
                        }
                    }
                }

                if (config.allArrows.booleanValue() || config.arrowWhitelist.contains(arrowKey)) {
                    // Set ammo use to 0 (infinite)
                    change.setValue(0.0F);
                }
            }
        }
    }
}
