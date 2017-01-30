package gigaherz.survivalist.api;

import com.google.common.collect.Lists;
import gigaherz.common.OreDictionaryHelper;
import gigaherz.survivalist.ConfigManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.Random;

public class Choppable
{
    public static abstract class ChoppingRecipe
    {
        private ItemStack output;
        private double outputMultiplier = 1.0;
        private double hitCountMultiplier = 1.0;
        private int maxOutput;

        public ChoppingRecipe(ItemStack output)
        {
            this.output = output;
        }

        public abstract boolean accepts(ItemStack stack);

        public ItemStack getOutput()
        {
            return output;
        }

        public double getOutputMultiplier()
        {
            return outputMultiplier;
        }

        public ChoppingRecipe setOutputMultiplier(double outputMultiplier)
        {
            this.outputMultiplier = outputMultiplier;
            return this;
        }

        public double getHitCountMultiplier()
        {
            return hitCountMultiplier;
        }

        public ChoppingRecipe setHitCountMultiplier(double hitCountMultiplier)
        {
            this.hitCountMultiplier = hitCountMultiplier;
            return this;
        }

        public int getMaxOutput()
        {
            return maxOutput;
        }

        public void setMaxOutput(int maxOutput)
        {
            this.maxOutput = maxOutput;
        }

        public ItemStack getResults(ItemStack input, EntityPlayer player, int axeLevel, int fortune, Random random)
        {
            double number = 0.4f * getOutputMultiplier();

            if (axeLevel >= 0)
                number = Math.max(0, getOutputMultiplier() * (1 + axeLevel)) * (1 + random.nextFloat() * fortune);

            int whole = (int) Math.floor(number);
            double remainder = number - whole;

            if (random.nextFloat() < remainder)
            {
                whole++;
            }

            if (getMaxOutput() > 0)
                whole = Math.min(whole, getMaxOutput());

            if (number > 0)
            {
                ItemStack out = getOutput().copy();
                out.setCount(whole);
                return out;
            }

            return ItemStack.EMPTY;
        }
    }

    public static class ChoppingItemRecipe extends ChoppingRecipe
    {
        private ItemStack input;

        public ChoppingItemRecipe(ItemStack input, ItemStack output)
        {
            super(output);
            this.input = input;
        }

        public ItemStack getInput()
        {
            return input;
        }

        public boolean accepts(ItemStack stack)
        {
            return OreDictionary.itemMatches(input, stack, false);
        }
    }

    public static class ChoppingOreRecipe extends ChoppingRecipe
    {
        private String oreName;

        public ChoppingOreRecipe(String oreName, ItemStack output)
        {
            super(output);
            this.oreName = oreName;
        }

        public String getOreName()
        {
            return oreName;
        }

        @Override
        public boolean accepts(ItemStack stack)
        {
            return OreDictionaryHelper.hasOreName(stack, oreName);
        }
    }

    public static final List<ChoppingRecipe> RECIPES = Lists.newArrayList();

    public static void registerStockRecipes()
    {
        registerRecipe("plankWood", new ItemStack(Items.STICK))
                .setOutputMultiplier(2.0);
        if (ConfigManager.instance.enableStringCrafting)
        {
            registerRecipe(new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.STRING))
                    .setMaxOutput(4);
        }
    }

    public static ChoppingRecipe registerRecipe(ItemStack input, ItemStack output)
    {
        return registerRecipe(new ChoppingItemRecipe(input, output));
    }

    public static ChoppingRecipe registerRecipe(String input, ItemStack output)
    {
        return registerRecipe(new ChoppingOreRecipe(input, output));
    }

    private static ChoppingRecipe registerRecipe(ChoppingRecipe recipe)
    {
        RECIPES.add(recipe);
        return recipe;
    }

    public static boolean isValidInput(ItemStack stack)
    {
        if (stack == null)
            return false;

        for (ChoppingRecipe recipe : RECIPES)
        {
            if (recipe.accepts(stack))
                return true;
        }

        return false;
    }

    public static ChoppingRecipe find(ItemStack stack)
    {
        if (stack == null)
            return null;

        for (ChoppingRecipe recipe : RECIPES)
        {
            if (recipe.accepts(stack))
                return recipe;
        }

        return null;
    }

    public static double getHitCountMultiplier(ItemStack stack)
    {
        if (stack == null)
            return 0;

        for (ChoppingRecipe recipe : RECIPES)
        {
            if (recipe.accepts(stack))
                return recipe.getHitCountMultiplier();
        }

        return 0;
    }
}
