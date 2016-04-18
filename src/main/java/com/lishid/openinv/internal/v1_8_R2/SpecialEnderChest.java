/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.internal.v1_8_R2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.ISpecialEnderChest;

// Volatile
import net.minecraft.server.v1_8_R2.EntityHuman;
import net.minecraft.server.v1_8_R2.IInventory;
import net.minecraft.server.v1_8_R2.InventoryEnderChest;
import net.minecraft.server.v1_8_R2.InventorySubcontainer;
import net.minecraft.server.v1_8_R2.ItemStack;

import org.bukkit.craftbukkit.v1_8_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftInventory;

public class SpecialEnderChest extends InventorySubcontainer implements IInventory, ISpecialEnderChest {

    private final OpenInv plugin;
    private final InventoryEnderChest enderChest;
    private final CraftInventory inventory = new CraftInventory(this);
    public List<HumanEntity> transaction = new ArrayList<HumanEntity>();
    public boolean playerOnline = false;
    private CraftPlayer owner;
    private int maxStack = MAX_STACK;

    public SpecialEnderChest(OpenInv plugin, Player p, Boolean online) {
        super(((CraftPlayer) p).getHandle().getEnderChest().getName(), ((CraftPlayer) p).getHandle().getEnderChest().hasCustomName(), ((CraftPlayer) p).getHandle().getEnderChest().getSize());
        this.plugin = plugin;
        CraftPlayer player = (CraftPlayer) p;
        this.enderChest = player.getHandle().getEnderChest();
        this.owner = player;
        this.items = enderChest.getContents();
    }

    @Override
    public Inventory getBukkitInventory() {
        return inventory;
    }

    @Override
    public boolean inventoryRemovalCheck(boolean save) {
        boolean offline = transaction.isEmpty() && !playerOnline;
        if (offline && save && !plugin.disableSaving()) {
            owner.saveData();
        }
        return offline;
    }

    @Override
    public void setPlayerOnline(Player player) {
        if (!playerOnline) {
            try {
                owner = (CraftPlayer) player;
                InventoryEnderChest playerEnderChest = owner.getHandle().getEnderChest();
                Field field = playerEnderChest.getClass().getField("items");
                field.setAccessible(true);
                field.set(playerEnderChest, this.items);
            }
            catch (Exception e) {}
            playerOnline = true;
        }
    }

    @Override
    public boolean setPlayerOffline() {
        playerOnline = false;
        return inventoryRemovalCheck(false);
    }

    @Override
    public ItemStack[] getContents() {
        return this.items;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
        this.inventoryRemovalCheck(true);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return this.owner;
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        return true;
    }

    public void startOpen() {

    }

    public void f() {

    }

    @Override
    public void update() {
        enderChest.update();
    }
}
