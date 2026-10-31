package kr.pokeachv.mixin;

import kr.pokeachv.PokeAchievements;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Chat (and other server messages) use the player's display name: put the chosen title in front of it. Name tags and
 * the tab list are drawn by the client from the plain name and stay unchanged.
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerDisplayNameMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void pokeachv$title(CallbackInfoReturnable<Text> cir) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            String title = PokeAchievements.titleOf(player.getUuid());

            if (title != null) {
                cir.setReturnValue(Text.literal("[" + title + "] ").formatted(Formatting.GOLD).append(cir.getReturnValue()));
            }
        }
    }
}
