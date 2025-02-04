package com.zergatul.cheatutils.wrappers;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collection;

public class WrapperRegistry<T> {

    private final IForgeRegistry<T> registry;

    public WrapperRegistry(IForgeRegistry<T> registry) {
        this.registry = registry;
    }

    public ResourceLocation getKey(T value) {
        return registry.getKey(value);
    }

    public T getValue(ResourceLocation id) {
        return registry.getValue(id);
    }

    public Collection<T> getValues() {
        return registry.getValues();
    }
}