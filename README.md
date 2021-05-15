# LifeLimitMC

LifeLimitMC is a Minecraft Plugin for Java Edition servers that use the Spigot API.

This is a plugin I wrote up over a span of a few days to refresh myself with Java. This plugin limits the number of lives players have on a server, and has multiple user-configurable options that allow server owners to tweak it to their likings.

By default, when players load into the server for the first time, they will be given 3 lives. Upon dying, they will be turned into a "ghost" (spectator mode), and their life count will be decreased by 1. They have the option to perform /respawn at any time to respawn into the world and continue playing. If a player runs out of lives, they will be unable to respawn until the plugin reloads (every 24 hours, the exact time is able to be changed in the config.yml file). By default, players waiting to respawn will only be able to talk to server operators and other players waiting to respawn. 


#Default configuration values:

reload_message: "Life Limit Reloaded!"

resetLivesTime: "00:00:00"

globalLivesTime: 1

firstTimeLoginCurrentLives: 3

autoRespawn: false

spectatorsMuted: true

opsSeeSpectators: true

playerDailyLives: 
- "UUID, Username, Life Count"

playerCurrentLives:
- "UUID, Username, Life Count"




Configuration Options: (config.yml)
reload_message: String. 
This value sets the message that will display upon performing /lifelimit reload

resetLivesTime: String. Formatted "hh:mm:ss"
This value sets the time that the plugin will reset everyones' lives to their daily cap

globalDefaultLives: Integer.
This values sets the number of lives a player will have by default when joining the server.

firstTimeLoginCurrentLives: Integer
This value sets the number of lives a player will have on their first day on the server.

autoRespawn: Boolean.
This value sets whether or not a player will automatically respawn, or whether they have to use the /respawn command

spectatorsMuted: Boolean.
This value sets whether or not a dead player will be able to have their chat seen by alive players.

opsSeeSpectators: Boolean.
If spectators are muted, this value sets whether or not ops can see dead players' chat, whether they are alive themselves or not.

playerDailyLives: [Plugin Data, do not modify]

playerCurrentLives: [Plugin Data, do not modify]
