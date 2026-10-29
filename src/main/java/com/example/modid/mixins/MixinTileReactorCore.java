package com.example.modid.mixins;

import com.brandon3055.draconicevolution.blocks.reactor.ProcessExplosion;
import com.brandon3055.draconicevolution.blocks.reactor.tileentity.TileReactorCore;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileReactorCore.class, remap = false)
public class MixinTileReactorCore {

    @Shadow private ProcessExplosion explosionProcess;
    @Shadow private int minExplosionDelay;

    @Unique private boolean modid$didInstantDetonate = false;

    @Inject(
            method = "updateCriticalState",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/brandon3055/brandonscore/handlers/ProcessHandler;addProcess(Lcom/brandon3055/brandonscore/handlers/IProcess;)V",
                            shift = At.Shift.AFTER),
            cancellable = true,
            remap = false)
    private void modid$instantBoom(CallbackInfo ci) {
        if (modid$didInstantDetonate) return;
        if (explosionProcess == null) return;

        // Use TileEntity getters (no shadows needed)
        TileReactorCore self = (TileReactorCore) (Object) this;
        World world = self.getWorld();
        BlockPos pos = self.getPos();

        if (!(world instanceof WorldServer)) return;

        modid$didInstantDetonate = true;

        // Remove the built-in delay
        minExplosionDelay = 0;

        // Finish ALL calculation immediately (WARNING: can freeze on big radii)
        while (!explosionProcess.isCalculationComplete()) {
            explosionProcess.updateCalculation();
        }

        // Detonate now
        explosionProcess.detonate();

        // Remove core block immediately
        world.setBlockToAir(pos);

        // Stop the rest of the method
        ci.cancel();
    }
}
