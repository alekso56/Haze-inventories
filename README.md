# Haze-inventories
Minimum core, allowing sql/flatfile based storage of inventories, per world per server inventories.

# Roadmap:
### Setup core
config
 > SQL settings, toggles. 
 
 >inactive player data time, default -1. (days) (for purge command)
 
 > auto delete inventories for worlds that are no longer loaded?
 
 
### information about sorting
 we will sort inventories in this structure for flatfile local storage.
 > circle -> worldname -> uuid.json/uuid.dat
 
 > Every single world is assigned to "unassigned" by default, these worlds will not share inventories with anything.

## Commonly requested features
> Store data in player.dat files, native to minecraft, alternativly, provide export to player.dat files

> conversion between all known plugins PWI/MULTIVERSE/Xinventories/MultiInv/etc

>  Storage of attributes and stats

> Integration with OpenINV/essentials

> cross server enderchest (sql)

> get player data from a database (e. g. MariaDB).

> prevent players from getting advancements in certain worlds

> clear inventories per world group/per world

> set login location of offline players per world group

> Override api for saving spesific things, other plugins can ask to not save a stat for a spesific player.

> per-gamemode-inv
 
### Setup events
Event  | comment
------------ | -------------
InventoryCreativeEvent  | deny if is syncing
PlayerChangedWorldEvent |
PlayerDeathEvent|
PlayerGameModeChangeEvent | this is considered a bad idea, and can be exploited, we will have this disabled by default.
PlayerQuitEvent|
PlayerRespawnEvent|
PlayerSpawnLocationEvent|
PlayerTeleportEvent|

### Setup API
Event  | comment
------------ | -------------
PreInventoryChangeEvent | Before the inventory is changed for the user
PostInventoryChangeEvent | 1 tick after the inv is changed
get Object for player (uuid based) > get circle from object
get circle from world > adjust circle

### Extrapolate required changes for third party plugins
