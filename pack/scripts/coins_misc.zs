#packmode normal

import mods.gregtech.recipe.RecipeMap;
import mods.gregtech.material.MaterialRegistry;
import crafttweaker.item.IItemStack;
  


recipes.addShapeless(<morefurnaces:furnaceblock:2> * 2, [<morefurnaces:furnaceblock:3>]); 
  
//Stone Widget
recipes.addShaped(<nomilabs:stonewidget>, [
	[null, <nomilabs:stonewidgetup>, null],
	[null, <enderio:item_material:10>, null], 
	[null, <nomilabs:stonewidgetdown>, null]]);
recipes.addShaped(<nomilabs:stonewidgetup>, [
	[<minecraft:stone_slab>, <ore:toolHammer>, <minecraft:stone_slab>],
	[<minecraft:stone>,<minecraft:stone>,<minecraft:stone>], 
	[<minecraft:stone>,<minecraft:stone>,<minecraft:stone>]]);
recipes.addShaped(<nomilabs:stonewidgetdown>, [
	[<minecraft:stone>,<minecraft:stone>,<minecraft:stone>], 
	[<minecraft:stone>,<minecraft:stone>,<minecraft:stone>],
	[<minecraft:stone_slab>, <ore:toolHammer>, <minecraft:stone_slab>]]);
recipes.remove(<enderio:item_material:10>);
recipes.addShaped(<enderio:item_material:10>,[
	[null, <minecraft:stone>, null],
	[<minecraft:stone>, <enderio:item_material:9>, <minecraft:stone>],
	[null, <minecraft:stone>, null]]);

	
	
//Wooden Widget
recipes.addShaped(<nomilabs:woodenwidget>, [
	[null, null, null],
	[<nomilabs:woodwidgetleft>, <enderio:item_material:9>, <nomilabs:woodwidgetright>],
	[null,null,null]]);
recipes.addShaped(<nomilabs:woodwidgetleft>, [
	[<ore:slabWood>,<ore:plankWood>,<ore:plankWood>], 
	[<ore:toolSaw>,<ore:plankWood>,<ore:plankWood>],
	[<ore:slabWood>, <ore:plankWood>, <ore:plankWood>]]);
recipes.addShaped(<nomilabs:woodwidgetright>, [
	[<ore:plankWood>,<ore:plankWood>,<ore:slabWood>], 
	[<ore:plankWood>,<ore:plankWood>,<ore:toolSaw>],
	[<ore:plankWood>,<ore:plankWood>,<ore:slabWood>]]);
recipes.remove(<enderio:item_material:9>);

	
//Alloy Widget
recipes.addShaped(<nomilabs:alloywidget>, [
	[<ore:cableGtSingleCopper>, <metaitem:plateCupronickel>, <ore:cableGtSingleCopper>],
	[<metaitem:plateCupronickel>, <metaitem:gearInvar>, <metaitem:plateCupronickel>], 
	[<ore:cableGtSingleCopper>, <metaitem:plateCupronickel>, <ore:cableGtSingleCopper>]]);


//Ender Widget
recipes.addShaped(<nomilabs:enderwidget>, [
	[<enderio:item_item_conduit>, <metaitem:nomilabs:platePulsatingIron>, <enderio:item_redstone_conduit>],
	[<metaitem:nomilabs:platePulsatingIron>, <metaitem:nomilabs:gearElectricalSteel>, <metaitem:nomilabs:platePulsatingIron>], 
	[<enderio:item_power_conduit>, <metaitem:nomilabs:platePulsatingIron>, <enderio:item_liquid_conduit:1>]]);





























//recipes.addShaped(<gregtech:ore_uranium_0> * 32, [[<nomilabs:nomicoin25>, null, null],[null, null, null], [null, null, <nomilabs:nomicoin25>]]);

































//recipes.addShaped(<gregtech:ore_lignite_0> * 32, [[<nomilabs:nomicoin>, <nomilabs:nomicoin>, <nomilabs:nomicoin>],[null, null, <nomilabs:nomicoin>], [<nomilabs:nomicoin>, null, null]]);




































/*
    Airtight Seal books for the low price of 900 Nomicoins!
 */

