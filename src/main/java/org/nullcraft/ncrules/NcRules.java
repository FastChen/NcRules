package org.nullcraft.ncrules;

import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class NcRules extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        setAllConfigWorldsRules();

        getLogger().info("NcRules Active");
        Bukkit.getPluginManager().registerEvents(this,this);

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        //子命令
        String[] subCommands = {"help", "reload", "list", "add"};
        if (args.length > 1) return new ArrayList<>();
        //如果此时仅输入了命令"sub"，则直接返回所有的子命令
        if (args.length == 0) return Arrays.asList(subCommands);
        //筛选所有可能的补全列表，并返回
        return Arrays.stream(subCommands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (label.equalsIgnoreCase("ncrules") || label.equalsIgnoreCase("ncrs")){
            if(args.length == 2){
                if (sender.hasPermission("ncrules.add") == true & args[0].equalsIgnoreCase("add") == true) {
                    if (Bukkit.getWorld(args[1]) == null){
                        sender.sendMessage(getConfig().getString("lang.prefix").replace("&", "§") + args[1]+ getConfig().getString("lang.notexistworld").replace("&","§"));
                        return true;
                    }
                    else {
                        reloadConfig();
                        List<String> worlds = getConfig().getStringList("enableworlds");
                        boolean haveworld = worlds.contains(args[1]);
                        if (haveworld == false){
                            worlds.add (args[1]);
                            getConfig().set("enableworlds",worlds);
                            saveConfig();
                            reloadConfig();
                            //设置刚添加世界的规则
                            setConfigGameRules(args[1]);
                            sender.sendMessage(getConfig().getString("lang.prefix").replace("&", "§") + getConfig().getString("lang.addworlddone").replace("&","§")+ args[1] );//+ haveworld
                            return true;
                        }else {
                            sender.sendMessage(getConfig().getString("lang.prefix").replace("&", "§") + args[1] + getConfig().getString("lang.existedworld").replace("&","§"));
                            return true;
                        }

                    }

                }else {sender.sendMessage(getConfig().getString("lang.prefix").replace("&", "§") + getConfig().getString("lang.notpermissions").replace("&","§"));return true;}
            }
            if(args.length == 1) {
                if (sender.hasPermission("ncrules.help") == true & args[0].equalsIgnoreCase("help") == true) {
                    sender.sendMessage(getConfig().getString("lang.prefix").replace("&", "§")+getConfig().getString("lang.commands").replace("&", "§"));
                    sender.sendMessage(getConfig().getString("lang.commandhelp").replace("&", "§"));
                    sender.sendMessage(getConfig().getString("lang.commandreload").replace("&", "§"));
                    sender.sendMessage(getConfig().getString("lang.commandlist").replace("&", "§"));
                    sender.sendMessage(getConfig().getString("lang.commandadd").replace("&", "§"));
                    return true;
                }

                if (sender.hasPermission("ncrules.reload") == true & args[0].equalsIgnoreCase("reload") == true) {
                    reloadConfig();
                    sender.sendMessage(getConfig().getString("lang.prefix").replace("&", "§") + getConfig().getString("lang.reloaddone").replace("&","§"));
                    setAllConfigWorldsRules();
                    return true;
                }

                if (sender.hasPermission("ncrules.list") == true & args[0].equalsIgnoreCase("list") == true) {
                    List<String> worlds = getConfig().getStringList("enableworlds");
                    String allworlds = new String();
                    if (worlds != null){
                        for (String s:worlds) {
                            allworlds = allworlds + s + ",";
                        }
                    }
                    sender.sendMessage(getConfig().getString("lang.prefix").replace("&", "§") + getConfig().getString("lang.activeworld").replace("&","§") + allworlds);
                    return true;
                }
            }
            sender.sendMessage(getConfig().getString("lang.prefix").replace("&", "§") + getConfig().getString("lang.gethelp").replace("&","§"));
            return true;
        }
        sender.sendMessage(getConfig().getString("lang.prefix").replace("&", "§") + getConfig().getString("lang.notpermissions").replace("&","§"));
        return false;
    }


    public void setAllConfigWorldsRules(){
        List<String> worlds = getConfig().getStringList("enableworlds");
        if (worlds != null){
            for (String s:worlds) {
                setConfigGameRules(s);
                Bukkit.broadcastMessage(getConfig().getString("lang.prefix").replace("&","§") + getConfig().getString("lang.changedrules").replace("&","§") + s + getConfig().getString("lang.changedworldrules").replace("&","§"));
            }
        }
    }

    public void setConfigGameRules(String world) {
        Set<String> rules = getConfig().getConfigurationSection("rules").getKeys(false);
        if (rules != null){
            for (String s:rules){
                Bukkit.getWorld(world).setGameRuleValue(s, getConfig().getString("rules."+s));
                //world.setGameRuleValue(s, getConfig().getString("rules."+s));
                //Bukkit.getPlayer("FastChen").sendMessage(s + " | " +getConfig().getString("rules."+s));
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e){
        if(getConfig().getBoolean("respawntitle.enabled")){
            Player playname = e.getEntity();
            //向死亡的玩家发送标题 内容可在confit.yml里更改
            playname.sendTitle(getConfig().getString("respawntitle.title").replace("&","§"),getConfig().getString("respawntitle.subtitle").replace("&","§"), 10, 70, 20);
        }
        return;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

