/*
 * Copyright (C) 2011-2018 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.internal.v1_4_5;

import com.lishid.openinv.internal.IPlayerDataManager;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.server.v1_4_5.EntityPlayer;
import net.minecraft.server.v1_4_5.ItemInWorldManager;
import net.minecraft.server.v1_4_5.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_4_5.CraftServer;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerDataManager implements IPlayerDataManager {

    @Override
    public Player loadPlayer(@NotNull OfflinePlayer offline) {
        // Ensure the player has data
        if (!offline.hasPlayedBefore()) {
            return null;
        }

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();

        // Create an entity to load the player data
        EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), offline.getName(),
                new ItemInWorldManager(server.getWorldServer(0)));

        // Get the bukkit entity
        Player target = entity.getBukkitEntity();
        if (target != null) {
            // Load data
            target.loadData();
        }
        // Return the entity
        return target;
    }

    @NotNull
	@Override
    public String getPlayerDataID(@NotNull OfflinePlayer offline) {
        return offline.getName();
    }

    @Override
    public OfflinePlayer getPlayerByID(@NotNull String identifier) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(identifier);
        // Ensure player is a real player, otherwise return null
        if (player == null || !player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }
        return player;
    }

    @NotNull
	@Override
    public Collection<? extends Player> getOnlinePlayers() {
        return Arrays.asList(Bukkit.getOnlinePlayers());
    }

    public static EntityPlayer getHandle(Player player) {
        if (player instanceof CraftPlayer) {
            return ((CraftPlayer) player).getHandle();
        }

        Server server = player.getServer();
        EntityPlayer nmsPlayer = null;

        if (server instanceof CraftServer) {
            nmsPlayer = ((CraftServer) server).getHandle().f(player.getName());
        }

        if (nmsPlayer == null) {
            // Could use reflection to examine fields, but it's honestly not worth the bother.
            throw new RuntimeException("Unable to fetch EntityPlayer from provided Player implementation");
        }

        return nmsPlayer;
    }

}
