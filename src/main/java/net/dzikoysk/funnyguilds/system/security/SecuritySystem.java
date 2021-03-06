package net.dzikoysk.funnyguilds.system.security;

import net.dzikoysk.funnyguilds.FunnyGuilds;
import net.dzikoysk.funnyguilds.basic.guild.Guild;
import net.dzikoysk.funnyguilds.basic.guild.Region;
import net.dzikoysk.funnyguilds.basic.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class SecuritySystem {

    private static SecuritySystem instance;
    private final List<User> blocked;

    public SecuritySystem() {
        instance = this;
        blocked = new ArrayList<>();
    }

    public static SecuritySystem getSecurity() {
        if (instance == null) {
            new SecuritySystem();
        }
        
        return instance;
    }

    public boolean checkPlayer(Player player, Object... values) {
        for (SecurityType type : SecurityType.values()) {
            if (checkPlayer(player, type, values)) {
                return true;
            }
        }
        
        return false;
    }

    public boolean checkPlayer(Player player, SecurityType type, Object... values) {
        if (! FunnyGuilds.getInstance().getPluginConfiguration().regionsEnabled) {
            return false;
        }
        
        if (isBanned(User.get(player))) {
            return true;
        }
        
        switch (type) {
            case FREECAM:
                Guild guild = null;

                for (Object value : values) {
                    if (value instanceof Guild) {
                        guild = (Guild) value;
                    }
                }

                if (guild == null) {
                    return false;
                }

                Region region = guild.getRegion();

                if (region == null) {
                    return false;
                }

                int distance = (int) region.getCenter().distance(player.getLocation());

                if (distance < 6) {
                    return false;
                }
                
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.isOp()) {
                        onlinePlayer.sendMessage(SecurityUtils.getBustedMessage(player.getName(), "FreeCam"));
                        onlinePlayer.sendMessage(SecurityUtils.getNoteMessage("Zaatakowal krysztal z odleglosci &c" + distance + " kratek"));
                    }
                }
                
                blocked.add(User.get(player));
                return true;
            case EVERYTHING:
                break;
            default:
                break;
        }
        
        return false;
    }

    public boolean isBanned(User user) {
        return blocked.contains(user);
    }

}
