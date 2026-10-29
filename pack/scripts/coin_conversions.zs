#packmode normal

// Each exchange preserves the denomination values: 1, 5, 25, and 100.
recipes.addShapeless(<nomilabs:nomicoin> * 5, [<nomilabs:nomicoin5>]);
recipes.addShapeless(<nomilabs:nomicoin5> * 5, [<nomilabs:nomicoin25>]);
recipes.addShapeless(<nomilabs:nomicoin25> * 4, [<nomilabs:nomicoin100>]);

recipes.addShaped(<nomilabs:nomicoin100>, [[<nomilabs:nomicoin25>, null, <nomilabs:nomicoin25>],[null,null,null], [<nomilabs:nomicoin25>, null, <nomilabs:nomicoin25>]]);
recipes.addShaped(<nomilabs:nomicoin25>, [[null, <nomilabs:nomicoin5>, null],[<nomilabs:nomicoin5>, <nomilabs:nomicoin5>, <nomilabs:nomicoin5>], [null, <nomilabs:nomicoin5>, null]]);
recipes.addShaped(<nomilabs:nomicoin5>, [[null, <nomilabs:nomicoin>, null],[<nomilabs:nomicoin>, <nomilabs:nomicoin>, <nomilabs:nomicoin>], [null, <nomilabs:nomicoin>, null]]);
