package com.littlecircleoo.tripwiredisarmer;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Tripwiredisarmer.MODID)
public class Tripwiredisarmer {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tripwiredisarmer";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public Tripwiredisarmer(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

    }

    @SubscribeEvent
    public void onShearsUsed(PlayerInteractEvent.RightClickBlock event) {
        // GetData
        ItemStack itemStack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos blockPos = event.getPos();
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();

        // Exceptional Condition
        if (!itemStack.is(Tags.Items.TOOLS_SHEAR) ||
                block != Blocks.TRIPWIRE ||
                !blockState.hasProperty(TripWireBlock.ATTACHED) ||
                !blockState.hasProperty(TripWireBlock.DISARMED) ||
                !blockState.hasProperty(TripWireBlock.POWERED)) {
            return;
        }

        // SetData
        if (block instanceof TripWireBlock wireBlock) {
            boolean attached = blockState.getValue(TripWireBlock.ATTACHED);
            boolean disarmed = blockState.getValue(TripWireBlock.DISARMED);
            // boolean powered = blockState.getValue(TripWireBlock.POWERED);
            if (attached && !disarmed /*&& !powered*/) {
                // level.playSound(event.getEntity(), blockPos, SoundEvents.TRIPWIRE_DETACH, SoundSource.BLOCKS); Deprecated: Directly changing BlockState cannot simulate the behavior of tripwire hooks when a tripwire is broken in Vanilla
                // VANILLA: attached=false,disarmed=true,powered=true
                BlockState newState = blockState.setValue(TripWireBlock.ATTACHED, false)
                        .setValue(TripWireBlock.DISARMED, true)
                        .setValue(TripWireBlock.POWERED, true);
                // level.setBlockAndUpdate(blockPos, newState); Deprecated: Directly changing BlockState cannot simulate the behavior of tripwire hooks when a tripwire is broken in Vanilla
                wireBlock.updateSource(level, blockPos, newState);
                event.getEntity().swing(event.getHand());
                itemStack.hurtAndBreak(1, event.getEntity(), LivingEntity.getSlotForHand(event.getHand()));
            }
        }
    }
}
