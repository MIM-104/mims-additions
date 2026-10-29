package com.example.modid.common;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityWalkingMachine extends Entity {

    private static final int WALK_TICKS = 164;
    private static final int RISE_TICKS = 18;
    private static final int DESCEND_TICKS = 36;
    private static final float DESCEND_OFFSET = -2.0F;
    private static final double WALK_SPEED = 0.2D;
    private static final float WALKER_HEIGHT = 3.25F;
    private static final float[] PATH_TURN_OFFSETS =
            new float[] {35.0F, -35.0F, 70.0F, -70.0F, 110.0F, -110.0F, 180.0F};

    private static final DataParameter<ItemStack> MACHINE_STACK =
            EntityDataManager.createKey(EntityWalkingMachine.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Float> WALK_DIRECTION =
            EntityDataManager.createKey(EntityWalkingMachine.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DESCENT_OFFSET =
            EntityDataManager.createKey(EntityWalkingMachine.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> WALK_AGE =
            EntityDataManager.createKey(EntityWalkingMachine.class, DataSerializers.VARINT);

    private int walkAge;
    private int machineBlockStateId = -1;
    private NBTTagCompound machineTileData;
    private BlockPos machineDestination;
    private double clientTargetX;
    private double clientTargetY;
    private double clientTargetZ;
    private float clientTargetYaw;
    private float clientTargetPitch;
    private int clientPositionUpdates;

    public EntityWalkingMachine(World world) {
        super(world);
        setSize(0.95F, WALKER_HEIGHT);
        stepHeight = 1.05F;
        noClip = false;
    }

    public EntityWalkingMachine(
            World world,
            double x,
            double y,
            double z,
            ItemStack machineStack,
            float walkDirection,
            IBlockState machineBlockState,
            NBTTagCompound machineTileData) {
        this(world);
        setPosition(x, y, z);
        dataManager.set(MACHINE_STACK, machineStack.copy());
        setWalkDirection(walkDirection);
        this.machineBlockStateId = Block.getStateId(machineBlockState);
        this.machineTileData = machineTileData.copy();
    }

    @Override
    protected void entityInit() {
        dataManager.register(MACHINE_STACK, ItemStack.EMPTY);
        dataManager.register(WALK_DIRECTION, 0.0F);
        dataManager.register(DESCENT_OFFSET, DESCEND_OFFSET);
        dataManager.register(WALK_AGE, 0);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        walkAge++;

        if (world.isRemote) {
            if (clientPositionUpdates > 0) {
                double stepX = (clientTargetX - posX) / clientPositionUpdates;
                double stepY = (clientTargetY - posY) / clientPositionUpdates;
                double stepZ = (clientTargetZ - posZ) / clientPositionUpdates;
                float stepYaw = MathHelper.wrapDegrees(clientTargetYaw - rotationYaw) / clientPositionUpdates;
                float stepPitch = (clientTargetPitch - rotationPitch) / clientPositionUpdates;
                setPositionAndRotation(
                        posX + stepX, posY + stepY, posZ + stepZ, rotationYaw + stepYaw, rotationPitch + stepPitch);
                clientPositionUpdates--;
            }
        } else {
            dataManager.set(WALK_AGE, walkAge);
            boolean walking = walkAge > RISE_TICKS && walkAge < WALK_TICKS;
            if (walking) {
                updateWalkDirection();
                float direction = dataManager.get(WALK_DIRECTION);
                motionX = getForwardX(direction) * WALK_SPEED;
                motionZ = getForwardZ(direction) * WALK_SPEED;
            } else {
                motionX = 0.0D;
                motionZ = 0.0D;
            }

            if (walkAge < WALK_TICKS) {
                if (walkAge <= RISE_TICKS) {
                    motionY = 0.0D;
                } else {
                    motionY -= 0.08D;
                    motionY = Math.max(motionY, -0.5D);
                }
                move(MoverType.SELF, motionX, motionY, motionZ);

                if (onGround) {
                    motionY = 0.0D;
                }
                if (walking && collidedHorizontally) {
                    setWalkDirection(findPathDirection(dataManager.get(WALK_DIRECTION)));
                }
            } else {
                if (machineDestination == null) {
                    machineDestination = findMachineDestination();
                }
                motionY = 0.0D;
            }

            if (walkAge >= WALK_TICKS + DESCEND_TICKS && restoreMachineAtDestination()) {
                setDead();
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotationDirect(
            double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        if (teleport) {
            setPositionAndRotation(x, y, z, yaw, pitch);
            clientPositionUpdates = 0;
            return;
        }

        clientTargetX = x;
        clientTargetY = y;
        clientTargetZ = z;
        clientTargetYaw = yaw;
        clientTargetPitch = pitch;
        clientPositionUpdates = Math.max(1, posRotationIncrements);
    }

    public ItemStack getMachineStack() {
        return dataManager.get(MACHINE_STACK);
    }

    public float getWalkDirection() {
        return dataManager.get(WALK_DIRECTION);
    }

    public float getWalkAnimation(float partialTicks) {
        return (getAnimationAge() + partialTicks) * 0.65F;
    }

    public float getLegSwing(float partialTicks) {
        float age = getAnimationAge() + partialTicks;
        if (age <= RISE_TICKS || age >= WALK_TICKS - 1) {
            return 0.0F;
        }
        return (float) Math.cos(getWalkAnimation(partialTicks)) * 0.35F;
    }

    public float getRiseOffset(float partialTicks) {
        float age = getAnimationAge() + partialTicks;
        if (age <= RISE_TICKS) {
            float progress = MathHelper.clamp(age / RISE_TICKS, 0.0F, 1.0F);
            return -1.0F + progress;
        }
        if (age >= WALK_TICKS) {
            float progress = MathHelper.clamp((age - WALK_TICKS) / DESCEND_TICKS, 0.0F, 1.0F);
            return dataManager.get(DESCENT_OFFSET) * progress;
        }
        return 0.0F;
    }

    private int getAnimationAge() {
        return world.isRemote ? dataManager.get(WALK_AGE) : walkAge;
    }

    private BlockPos findMachineDestination() {
        BlockPos basePos = new BlockPos(posX, posY, posZ);
        int[] yOffsets = {-2, -1, 0, 1, 2, 3, 4};
        for (int yOffset : yOffsets) {
            BlockPos destination = basePos.up(yOffset);
            if (world.isAirBlock(destination) || world.getBlockState(destination).getMaterial().isReplaceable()) {
                return destination;
            }
        }
        return null;
    }

    private boolean restoreMachineAtDestination() {
        if (machineBlockStateId < 0 || machineTileData == null || machineDestination == null) {
            return false;
        }

        IBlockState machineBlockState = Block.getStateById(machineBlockStateId);
        if (!machineBlockState.getBlock().hasTileEntity(machineBlockState)) {
            return false;
        }

        if (!world.isAirBlock(machineDestination)
                && !world.getBlockState(machineDestination).getMaterial().isReplaceable()) {
            return false;
        }

        NBTTagCompound tileData = machineTileData.copy();
        tileData.setInteger("x", machineDestination.getX());
        tileData.setInteger("y", machineDestination.getY());
        tileData.setInteger("z", machineDestination.getZ());
        TileEntity restoredTile = TileEntity.create(world, tileData);
        if (restoredTile == null || !world.setBlockState(machineDestination, machineBlockState, 3)) {
            return false;
        }

        world.setTileEntity(machineDestination, restoredTile);
        return true;
    }

    private void updateWalkDirection() {
        float direction = dataManager.get(WALK_DIRECTION);
        if (walkAge % 8 == 0 && !isPathClear(direction)) {
            setWalkDirection(findPathDirection(direction));
        }
    }

    private float findPathDirection(float currentDirection) {
        for (float offset : PATH_TURN_OFFSETS) {
            float candidate = currentDirection + offset;
            if (isPathClear(candidate)) {
                return candidate;
            }
        }

        return currentDirection + (rand.nextBoolean() ? 90.0F : -90.0F);
    }

    private boolean isPathClear(float direction) {
        double xOffset = getForwardX(direction) * 0.9D;
        double zOffset = getForwardZ(direction) * 0.9D;
        AxisAlignedBB ahead = getEntityBoundingBox().offset(xOffset, 0.0D, zOffset);
        return world.getCollisionBoxes(this, ahead).isEmpty()
                || world.getCollisionBoxes(this, ahead.offset(0.0D, 1.0D, 0.0D)).isEmpty();
    }

    private void setWalkDirection(float direction) {
        float normalizedDirection = MathHelper.wrapDegrees(direction);
        dataManager.set(WALK_DIRECTION, normalizedDirection);
        rotationYaw = normalizedDirection;
        prevRotationYaw = normalizedDirection;
    }

    private double getForwardX(float direction) {
        return -MathHelper.sin((float) Math.toRadians(direction));
    }

    private double getForwardZ(float direction) {
        return MathHelper.cos((float) Math.toRadians(direction));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        dataManager.set(
                MACHINE_STACK,
                compound.hasKey("MachineStack")
                        ? new ItemStack(compound.getCompoundTag("MachineStack"))
                        : ItemStack.EMPTY);
        setWalkDirection(compound.getFloat("WalkDirection"));
        walkAge = compound.getInteger("WalkAge");
        if (compound.hasKey("MachineBlockState")) {
            machineBlockStateId = compound.getInteger("MachineBlockState");
            machineTileData = compound.getCompoundTag("MachineTileData").copy();
        }
        if (compound.hasKey("MachineDestination")) {
            machineDestination = BlockPos.fromLong(compound.getLong("MachineDestination"));
        }
        if (compound.hasKey("DescentOffset")) {
            dataManager.set(DESCENT_OFFSET, compound.getFloat("DescentOffset"));
        }
        dataManager.set(WALK_AGE, walkAge);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (!dataManager.get(MACHINE_STACK).isEmpty()) {
            compound.setTag("MachineStack", dataManager.get(MACHINE_STACK).writeToNBT(new NBTTagCompound()));
        }
        compound.setFloat("WalkDirection", dataManager.get(WALK_DIRECTION));
        compound.setInteger("WalkAge", walkAge);
        compound.setInteger("SyncedWalkAge", dataManager.get(WALK_AGE));
        if (machineBlockStateId >= 0 && machineTileData != null) {
            compound.setInteger("MachineBlockState", machineBlockStateId);
            compound.setTag("MachineTileData", machineTileData.copy());
        }
        if (machineDestination != null) {
            compound.setLong("MachineDestination", machineDestination.toLong());
        }
        compound.setFloat("DescentOffset", dataManager.get(DESCENT_OFFSET));
    }
}
