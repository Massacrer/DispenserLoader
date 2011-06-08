This is a plugin for Bukkit, which makes it much easier to work with dispensers en masse. See the command reference below, or the Bukkit forum thread.

dload---------------------------Toggles whether or not the plugin is active for a specific user
|
|-------help--------------------Shows some basic usage examples
|
|-------wand--------------------Gives player their wand item, for example if they lost it (it is 
|				also given to them when the plugin is enabled for them)
|
|-------setwand (number)--------Sets the player's wand item to the item specified by the number entered
|
|-------empty (single mode)-----Toggles single-dispenser emptying mode. In this mode, clicking a dispenser 
|	|			will remove everything from its inventory
|	|
|	|-------once------------Optional. If used (e.g. /dload empty once), empty mode will only be used for 
|	|			the next dispenser to be hit, and then disabled.
|	|
|	|-(area mode)-----------Empties all dispensers in the region selected by the /dload area command
|
|-------fill (single mode)------Toggles single-dispenser filling mode. In this mode, clicking a dispenser 
|	|			will fill all available slots in its inventory with the player's item
|	|
|	|-------once------------Optional. Same as for /dload empty once. These are just laziness / 
|	|			optimisation things :)
|	|
|	|---(area mode)---------Fills up every dispenser in the selection region
|
|-------add (single mode)-------Sets mode to single-block adding mode, with user-customised material and amounts staying set.
|	|
|	|---(area mode)---------Adds the player's amount of material to the dispensers in the selection region.
|
|-------reset-------------------Resets player's options to defaults. These are normal single-dispenser 
|				mode, adding 64 arrows per hit
|
|-------arrows------------------Resets player's item and amount to 64 arrows.
|
|-------info--------------------Shows player's information: fill mode, empty mode, area mode, item and amount. 
|				Note: if both fill and empty are set for single-dispenser mode, the dispenser 
|				will first be emptied and then fully filled with the player's material
|
|-------area--------------------Toggles area selection mode. In this mode, click two blocks to form a cuboid 
|				around the target dispensers.
|
|-------(number)----------------Sets the player's item type to the entered item number. Player's amount is set to 
|				64 unless already defined.
|
|-------(number) (number)-------Sets the player's item type to the first number, and amount to the second.