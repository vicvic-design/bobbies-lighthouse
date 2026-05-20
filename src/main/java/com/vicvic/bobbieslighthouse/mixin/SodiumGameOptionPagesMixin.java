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
import net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl;
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
    private static void bobbieslighthouse$addBobbyFilterCutoffSlider(CallbackInfoReturnable<OptionPage> cir) {
        LodestoneFarConfig config = LodestoneFarClient.config();
        if (config == null) {
            return;
        }
        OptionPage page = cir.getReturnValue();
        List<OptionGroup> groups = new ArrayList<>(page.getGroups());
        groups.add(OptionGroup.createBuilder()
                .add(OptionImpl.createBuilder(integerClass(), new LighthouseConfigStorage())
                        .setName(Component.translatable("option.bobbieslighthouse.bobby_filter_cutoff"))
                        .setTooltip(Component.translatable("tooltip.option.bobbieslighthouse.bobby_filter_cutoff"))
                        .setControl(option -> new SliderControl(option, 2, 64, 1, ControlValueFormatter.number()))
                        .setBinding(LodestoneFarConfig::setBobbyFilterCutoffChunks, loaded -> loaded.bobbyFilterCutoffChunks)
                        .setImpact(OptionImpact.LOW)
                        .build())
                .build());
        cir.setReturnValue(new OptionPage(page.getName(), ImmutableList.copyOf(groups)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Class<Integer> integerClass() {
        return (Class) Integer.TYPE;
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
