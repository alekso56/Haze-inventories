# Haze-inventories
Minimum core, allowing sql/flatfile based storage of inventories, per world per server inventories.

# Roadmap:
### Setup core
config
 SQL settings, toggles. 
 
### information about sorting
 we will sort inventories in this structure for flatfile local storage.
 circle -> worldname -> username -> data.json/data.dat
 Every single world is assigned to "unassigned" by default, these worlds will not share inventories with anything.
 
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
> get Object for player (uuid based) > get circle from object
> get circle from world > adjust circle

### Extrapolate required changes for third party plugins
