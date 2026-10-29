package com.example.modid.firmware;

import com.example.modid.MimsAdditions;
import com.example.modid.Tags;
import com.example.modid.common.ModCreativeTabs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class FirmwareContent {
    public static final Block WORKBENCH = new Workbench();
    public static final Block LOGIC_CASING = metal("etched_logic_casing");
    public static final Block MEMORY_BANK = metal("firmware_memory_bank");
    public static final Block NOETIC_COIL = metal("noetic_induction_coil");
    public static final Item SOLDERING_IRON = tool("precision_soldering_iron", 320);
    public static final Item WIRE_STRIPPERS = tool("precision_wire_strippers", 320);

    private FirmwareContent() {}

    private static Block metal(String name) {
        return new Block(Material.IRON)
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setHardness(4.0F)
                .setResistance(15.0F)
                .setSoundType(SoundType.METAL)
                .setCreativeTab(ModCreativeTabs.MIMS_ADDITIONS);
    }

    private static Item tool(String name, int durability) {
        return new Item()
                .setRegistryName(Tags.MOD_ID, name)
                .setTranslationKey(Tags.MOD_ID + "." + name)
                .setMaxStackSize(1)
                .setMaxDamage(durability)
                .setCreativeTab(ModCreativeTabs.MIMS_ADDITIONS);
    }

    @SubscribeEvent
    public static void blocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(WORKBENCH, LOGIC_CASING, MEMORY_BANK, NOETIC_COIL);
    }

    @SubscribeEvent
    public static void items(RegistryEvent.Register<Item> event) {
        for (Block block : new Block[] {WORKBENCH, LOGIC_CASING, MEMORY_BANK, NOETIC_COIL}) {
            event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }
        event.getRegistry().registerAll(SOLDERING_IRON, WIRE_STRIPPERS);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void recipes(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(FirmwareEraseRecipe.INSTANCE.setRegistryName(Tags.MOD_ID, "erase_firmware"));
    }

    private static final class Workbench extends BlockContainer {
        private static final PropertyDirection FACING = BlockHorizontal.FACING;

        private Workbench() {
            super(Material.IRON);
            setRegistryName(Tags.MOD_ID, "precision_firmware_workbench");
            setTranslationKey(Tags.MOD_ID + ".precision_firmware_workbench");
            setCreativeTab(ModCreativeTabs.MIMS_ADDITIONS);
            setHardness(3.0F);
            setResistance(10.0F);
            setSoundType(SoundType.METAL);
            setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        }

        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, FACING);
        }

        @Override
        public IBlockState getStateFromMeta(int meta) {
            return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
        }

        @Override
        public int getMetaFromState(IBlockState state) {
            return state.getValue(FACING).getHorizontalIndex();
        }

        @Override
        public IBlockState getStateForPlacement(
                World world,
                BlockPos pos,
                EnumFacing side,
                float x,
                float y,
                float z,
                int meta,
                EntityLivingBase placer,
                EnumHand hand) {
            return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
        }

        @Override
        public TileEntity createNewTileEntity(World world, int meta) {
            return new FirmwareWorkbenchTile();
        }

        @Override
        public EnumBlockRenderType getRenderType(IBlockState state) {
            return EnumBlockRenderType.MODEL;
        }

        @Override
        public boolean isOpaqueCube(IBlockState state) {
            return false;
        }

        @Override
        public boolean isFullCube(IBlockState state) {
            return false;
        }

        @Override
        public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
            return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        }

        @Override
        public boolean onBlockActivated(
                World world,
                BlockPos pos,
                IBlockState state,
                EntityPlayer player,
                EnumHand hand,
                EnumFacing facing,
                float hitX,
                float hitY,
                float hitZ) {
            if (!world.isRemote) player.openGui(MimsAdditions.INSTANCE, 41, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }

        @Override
        public void breakBlock(World world, BlockPos pos, IBlockState state) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof FirmwareWorkbenchTile) {
                FirmwareWorkbenchTile bench = (FirmwareWorkbenchTile) tile;
                for (int i = 0; i < bench.items.getSlots(); i++) {
                    InventoryHelper.spawnItemStack(
                            world, pos.getX(), pos.getY(), pos.getZ(), bench.items.getStackInSlot(i));
                }
            }
            super.breakBlock(world, pos, state);
        }
    }
}
