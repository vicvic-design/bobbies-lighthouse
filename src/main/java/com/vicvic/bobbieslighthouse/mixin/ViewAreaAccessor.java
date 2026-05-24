package com.vicvic.bobbieslighthouse.mixin;

import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ViewArea.class)
public interface ViewAreaAccessor {
    @Invoker("getRenderSection")
    SectionRenderDispatcher.RenderSection bobbieslighthouse$getRenderSection(long sectionNode);
}
