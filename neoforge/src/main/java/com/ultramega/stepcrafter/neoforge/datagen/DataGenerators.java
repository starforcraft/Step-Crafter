package com.ultramega.stepcrafter.neoforge.datagen;

import com.ultramega.stepcrafter.neoforge.datagen.loot.LootTableProviderImpl;
import com.ultramega.stepcrafter.neoforge.datagen.model.ModelProviders;
import com.ultramega.stepcrafter.neoforge.datagen.recipe.MainRecipeProvider;
import com.ultramega.stepcrafter.neoforge.datagen.recipe.RecoloringRecipeProvider;
import com.ultramega.stepcrafter.neoforge.datagen.tag.BlockTagsProvider;
import com.ultramega.stepcrafter.neoforge.datagen.tag.ItemTagsProvider;

import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static com.ultramega.stepcrafter.common.StepCrafterIdentifierUtil.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent.Client e) {
        final DataGenerator generator = e.getGenerator();
        final DataGenerator.PackGenerator pack = generator.getVanillaPack(true);
        pack.addProvider(ModelProviders::new);
        pack.addProvider(output -> new LootTableProviderImpl(output, e.getLookupProvider()));
        pack.addProvider(output -> new RecoloringRecipeProvider.Runner(output, e.getLookupProvider()));
        pack.addProvider(output -> new MainRecipeProvider.Runner(output, e.getLookupProvider()));
        final BlockTagsProvider blockTagsProvider = pack.addProvider(output -> new BlockTagsProvider(output, e.getLookupProvider()));
        pack.addProvider(output -> new ItemTagsProvider(output, e.getLookupProvider(), blockTagsProvider.contentsGetter()));
    }
}
