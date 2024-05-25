# How to block this mod?
If you are the server owner, and you don't want your players to use this mod, you can detect
them by channel `hcscr:haram` and block by sending a single boolean  to the client on this channel.
`true` (byte: `1`) means the mod should be blocked, `false` (byte: `0`) means the mod should be unblocked,
this can be changed in-game multiple times per one session.

The client will receive a *toast* notification, if you've (un-)blocked this mod.

**NOTE**: This project is open source and any forks and third-party modifications can remove this form of protection.

## Example Bukkit Plugin
```java
public class ExampleBlocker extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this); // Register this class as a listener for event handlers below.
        getServer().getMessenger().registerOutgoingPluginChannel(this, "hcscr:haram"); // Register mod's channel for sending messages.
    }

    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent e) {
        if (e.getChannel().equals("hcscr:haram")) { // The player is using the mod.
            e.getPlayer().sendMessage("You're using the HCsCR mod!"); // Do anything with this mod.
            e.getPlayer().sendPluginMessage(this, "hcscr:haram", new byte[] {1}); // Block the mod via '1' (`true`) byte. (You can unblock by sending '0' (`false`))
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
        if (e.getWorld().getName().equals("example_world_where_we_should_unblock_the_mod")) { // Example usage case.
            e.getPlayer().sendPluginMessage(this, "hcscr:haram", new byte[] {0}); // Unblock the mod via '0' (`false`) byte.
        } else {
            e.getPlayer().sendPluginMessage(this, "hcscr:haram", new byte[] {1}); // Block the mod via '1' (`true`) byte.
        }
    }
}
```