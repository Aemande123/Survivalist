package gigaherz.survivalist.rack;

import gigaherz.common.BlockRegistered;
import gigaherz.survivalist.ConfigManager;
import gigaherz.survivalist.GuiHandler;
import gigaherz.survivalist.Survivalist;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class BlockRack extends BlockRegistered
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockRack(String name)
    {
        super(name, Material.WOOD);
        setUnlocalizedName(Survivalist.MODID + ".rack");
        setCreativeTab(CreativeTabs.DECORATIONS);
        setSoundType(SoundType.WOOD);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setHardness(1.0F);
        setResistance(1.0F);
        setLightOpacity(0);
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        if (ConfigManager.instance.enableDryingRack)
        {
            super.getSubBlocks(itemIn, tab, list);
        }
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileRack();
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }

    @Deprecated
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(FACING, EnumFacing.HORIZONTALS[meta & 3]);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).ordinal();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack)
    {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = worldIn.getTileEntity(pos);

        if (!(tileEntity instanceof TileRack) || playerIn.isSneaking())
            return false;

        playerIn.openGui(Survivalist.instance, GuiHandler.GUI_RACK, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileRack)
        {
            dropInventoryItems(worldIn, pos, ((TileRack) tileentity).inventory());
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    private static void dropInventoryItems(World worldIn, BlockPos pos, IItemHandler inventory)
    {
        for (int i = 0; i < inventory.getSlots(); ++i)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);

            if (itemstack != null)
            {
                InventoryHelper.spawnItemStack(worldIn, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), itemstack);
            }
        }
    }
}
