# Haze-inventories
Minimum core, allowing sql/flatfile based storage of inventories, per circle per server inventories.

# Roadmap:
### Setup core
config
 > SQL settings, toggles. 
 
 >inactive player data time, default -1. (days) (for purge command)
 
 > auto delete inventories for worlds that are no longer loaded?
 
 
### information about sorting
 we will sort inventories in this structure for flatfile local storage.
 > circle -> worldname -> uuid.json/uuid.dat
 > playermeta -> uuid.json
 
 > Every single world is assigned to "unassigned" by default, these worlds will not share inventories with anything.

## Commonly requested features
> conversion between all known plugins PWI/MULTIVERSE/Xinventories/MultiInv/etc

> Integration with OpenINV/essentials

> cross server enderchest (sql)

> get player data from a database (e. g. MariaDB).

> prevent players from getting advancements in certain worlds

> clear inventories per world group/per world

> Override api for saving spesific things, other plugins can ask to not save a stat for a spesific player.


### Setup API
Event  | comment
------------ | -------------
PreInventoryChangeEvent | Before the inventory is changed for the user
PostInventoryChangeEvent | 1 tick after the inv is changed
get Object for player (uuid based) > get circle from object
get circle from world > adjust circle