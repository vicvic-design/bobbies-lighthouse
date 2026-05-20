package com.vicvic.bobbieslighthouse.mixin;

import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.ViewArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SectionOcclusionGraph.class)
public interface SectionOcclusionGraphAccessor {
    @Accessor("viewArea")
    ViewArea bobbieslighthouse$getViewArea();
}
