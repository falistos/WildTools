package com.bgsoftware.wildtools.objects.tools;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.objects.ToolMode;
import com.bgsoftware.wildtools.api.objects.tools.FluidBuilderTool;
import com.bgsoftware.wildtools.utils.blocks.BlocksController;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class WFluidBuilderTool extends WTool implements FluidBuilderTool {

    private static final List<String> NON_TRANSPARENT_BLOCKS = Arrays.asList("WEB", "COBWEB");

    private final int length;

    public WFluidBuilderTool(Material type, String name, int length){
        super(type, name, ToolMode.FLUID_BUILDER);
        this.length = length;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public boolean onBlockInteract(PlayerInteractEvent e) {
        e.setCancelled(true);

        BlockFace blockFace = e.getBlockFace();
        if (blockFace != BlockFace.UP)
            return false;

        String liquidName = null;
        Material liquidType = null;
        Iterator<String> materialIterator = getWhitelistedMaterials().iterator();
        if (materialIterator.hasNext()) {
            liquidName = materialIterator.next();
            liquidType = Material.getMaterial(liquidName);
        }

        if (liquidType == null) {
            WildToolsPlugin.getPlugin().getLogger().info("No valid liquid type defined : "+liquidName);
            return false;
        }

        BlocksController blocksController = new BlocksController();
        boolean usingDurability = isUsingDurability();
        int toolIterations = Math.min(usingDurability ? getDurability(e.getPlayer(), e.getItem()) : length, length);
        int iter;

        Block nextBlock = e.getClickedBlock();

        for (iter = 0; iter < toolIterations; iter++) {
            nextBlock = nextBlock.getRelative(blockFace);
            Material nextBlockType = nextBlock.getType();

            if (nextBlockType.isSolid() ||
                    NON_TRANSPARENT_BLOCKS.contains(nextBlockType.name()) ||
                    nextBlock.getY() >= nextBlock.getWorld().getMaxHeight() ||
                    !WildToolsPlugin.getPlugin().getProviders().isInsideClaim(e.getPlayer(), nextBlock.getLocation()))
                break;

            blocksController.setType(nextBlock.getLocation(), liquidType.getId());
        }

        blocksController.updateSession();

        if (iter > 0)
            reduceDurablility(e.getPlayer(), usingDurability ? iter : 1, e.getItem());

        return true;
    }
}