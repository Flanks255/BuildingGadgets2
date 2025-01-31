package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Config;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import com.direwolf20.buildinggadgets2.util.modes.Cut;
import com.direwolf20.buildinggadgets2.util.modes.Paste;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GadgetCutPaste extends BaseGadget {
    public GadgetCutPaste() {
        super();
    }

    @Override
    public int getEnergyMax() {
        return Config.CUTPASTEGADGET_MAXPOWER.get();
    }

    @Override
    public int getEnergyCost() {
        return Config.CUTPASTEGADGET_COST.get();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Minecraft mc = Minecraft.getInstance();

        if (level == null || mc.player == null) {
            return;
        }

        boolean sneakPressed = Screen.hasShiftDown();

        if (sneakPressed) {

        }
    }

    @Override
    InteractionResultHolder<ItemStack> onAction(ItemActionContext context) {
        var gadget = context.stack();

        var mode = GadgetNBT.getMode(gadget);
        if (mode.getId().getPath().equals("cut")) {
            GadgetNBT.setCopyStartPos(gadget, context.pos());
            //buildAndStore(context, gadget);
        } else if (mode.getId().getPath().equals("paste")) {
            UUID uuid = GadgetNBT.getUUID(gadget);
            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(context.player().level().getServer()).overworld());
            ArrayList<StatePos> buildList = bg2Data.getCopyPasteList(uuid, true);
            ArrayList<TagPos> tagList = bg2Data.getTEMap(uuid);

            // This should go through some translation based process
            // mode -> beforeBuild (validation) -> scheduleBuild / Build -> afterBuild (cleanup & use of items etc)
            ArrayList<StatePos> actuallyBuiltList = BuildingUtils.buildWithTileData(context.level(), context.player(), buildList, getHitPos(context).above(), tagList, gadget);
            if (!actuallyBuiltList.isEmpty())
                GadgetNBT.clearAnchorPos(gadget);
            GadgetNBT.clearCopyUUID(gadget); // Erase copy UUID so the user doesn't get the 'are you sure' prompt
            GadgetNBT.setMode(gadget, new Cut()); // Set it back to cut mode - no need to stay in paste since the paste is gone :)
            return InteractionResultHolder.success(gadget);
        } else {
            return InteractionResultHolder.pass(gadget);
        }

        return InteractionResultHolder.success(gadget);
    }

    /**
     * Selects the block assuming you're actually looking at one
     */
    @Override
    InteractionResultHolder<ItemStack> onShiftAction(ItemActionContext context) {
        var gadget = context.stack();

        var mode = GadgetNBT.getMode(gadget);
        if (mode.getId().getPath().equals("cut")) {
            GadgetNBT.setCopyEndPos(gadget, context.pos());
            //buildAndStore(context, gadget);
        } else if (mode.equals(new Paste())) {
            //Paste
        } else {
            return InteractionResultHolder.pass(gadget);
        }

        return InteractionResultHolder.success(gadget);
    }

    public void cutAndStore(Player player, ItemStack gadget) {
        //TODO Avoid iterating the blocks 3x
        ArrayList<StatePos> buildList = new Cut().collect(Direction.UP, player, BlockPos.ZERO, Blocks.AIR.defaultBlockState());
        ArrayList<TagPos> teData = new Cut().collectTileData(player);
        if (buildList.isEmpty()) return;
        UUID uuid = GadgetNBT.getUUID(gadget);
        GadgetNBT.setCopyUUID(gadget); //This UUID will be used to determine if the copy/paste we are rendering from the cache is old or not.
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(player.level().getServer()).overworld());
        bg2Data.addToCopyPaste(uuid, buildList);
        bg2Data.addToTEMap(uuid, teData);
        new Cut().removeBlocks(player);
        player.displayClientMessage(Component.translatable("buildinggadgets2.messages.cutblocks", buildList.size()), true);
        GadgetNBT.setCopyStartPos(gadget, GadgetNBT.nullPos);
        GadgetNBT.setCopyEndPos(gadget, GadgetNBT.nullPos);
    }

    /**
     * Used to retrieve the correct building modes in various places
     */
    @Override
    public GadgetTarget gadgetTarget() {
        return GadgetTarget.CUTPASTE;
    }
}
