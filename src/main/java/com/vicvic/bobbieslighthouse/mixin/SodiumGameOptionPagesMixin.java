package com.vicvic.bobbieslighthouse.mixin;

import com.google.common.collect.ImmutableList;
import com.vicvic.bobbieslighthouse.LodestoneFarClient;
import com.vicvic.bobbieslighthouse.config.LodestoneFarConfig;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages;
import net.caffeinemc.mods.sodium.client.gui.options.OptionGroup;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpact;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import net.caffeinemc.mods.sodium.client.gui.options.OptionPage;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.caffeinemc.mods.sodium.client.gui.options.control.CyclingControl;
import net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl;
import net.caffeinemc.mods.sodium.client.gui.options.control.TickBoxControl;
import net.caffeinemc.mods.sodium.client.gui.options.storage.OptionStorage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = SodiumGameOptionPages.class, remap = false)
public abstract class SodiumGameOptionPagesMixin {
    @Inject(method = "general", at = @At("RETURN"), cancellable = true)
    private static void bobbieslighthouse$addLighthouseOptions(CallbackInfoReturnable<OptionPage> cir) {
        LodestoneFarConfig config = LodestoneFarClient.config();
        if (config == null) {
            return;
        }
        OptionPage page = cir.getReturnValue();
        List<OptionGroup> groups = new ArrayList<>(page.getGroups());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(booleanClass(), new LighthouseConfigStorage())
                        .setName(Component.translatable("option.bobbieslighthouse.enabled"))
                        .setTooltip(Component.translatable("tooltip.option.bobbieslighthouse.enabled"))
                        .setControl(TickBoxControl::new)
                        .setBinding(LodestoneFarConfig::setEnabled, loaded -> loaded.enabled)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(integerClass(), new LighthouseConfigStorage())
                        .setName(Component.translatable("option.bobbieslighthouse.anchor_radius"))
                        .setTooltip(Component.translatable("tooltip.option.bobbieslighthouse.anchor_radius"))
                        .setControl(option -> new SliderControl(option, 0, 32, 1, ControlValueFormatter.number()))
                        .setBinding(LodestoneFarConfig::setAnchorRadiusChunks, loaded -> loaded.anchorRadiusChunks)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(integerClass(), new LighthouseConfigStorage())
                        .setName(Component.translatable("option.bobbieslighthouse.lighthouse_range"))
                        .setTooltip(Component.translatable("tooltip.option.bobbieslighthouse.lighthouse_range"))
                        .setControl(option -> new SliderControl(option, 128, 1024, 128, ControlValueFormatter.number()))
                        .setBinding(LodestoneFarConfig::setLighthouseRangeChunks, loaded -> loaded.lighthouseRangeChunks)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(integerClass(), new LighthouseConfigStorage())
                        .setName(Component.translatable("option.bobbieslighthouse.max_active_anchors"))
                        .setTooltip(Component.translatable("tooltip.option.bobbieslighthouse.max_active_anchors"))
                        .setControl(option -> new SliderControl(option, 2, 64, 2, ControlValueFormatter.number()))
                        .setBinding(LodestoneFarConfig::setMaxActiveAnchors, loaded -> loaded.maxActiveAnchors)
                        .setImpact(OptionImpact.MEDIUM)
                        .build())
                .add(OptionImpl.createBuilder(integerClass(), new LighthouseConfigStorage())
                        .setName(Component.translatable("option.bobbieslighthouse.max_extra_chunks"))
                        .setTooltip(Component.translatable("tooltip.option.bobbieslighthouse.max_extra_chunks"))
                        .setControl(option -> new SliderControl(option, 64, 4096, 64, ControlValueFormatter.number()))
                        .setBinding(LodestoneFarConfig::setMaxExtraRenderedChunks, loaded -> loaded.maxExtraRenderedChunks)
                        .setImpact(OptionImpact.HIGH)
                        .build())
                .add(OptionImpl.createBuilder(shapeClass(), new LighthouseConfigStorage())
                        .setName(Component.translatable("option.bobbieslighthouse.shape"))
                        .setTooltip(Component.translatable("tooltip.option.bobbieslighthouse.shape"))
                        .setControl(option -> new CyclingControl<>(option, LodestoneFarConfig.Shape.class))
                        .setBinding(LodestoneFarConfig::setShape, loaded -> loaded.shape)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .build());
        cir.setReturnValue(new OptionPage(page.getName(), ImmutableList.copyOf(groups)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Class<Integer> integerClass() {
        return (Class) Integer.TYPE;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Class<Boolean> booleanClass() {
        return (Class) Boolean.TYPE;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Class<LodestoneFarConfig.Shape> shapeClass() {
        return (Class) LodestoneFarConfig.Shape.class;
    }

    private static final class LighthouseConfigStorage implements OptionStorage<LodestoneFarConfig> {
        @Override
        public LodestoneFarConfig getData() {
            return LodestoneFarClient.config();
        }

        @Override
        public void save() {
            LodestoneFarConfig config = LodestoneFarClient.config();
            if (config != null) {
                config.save();
            }
        }
    }
}
