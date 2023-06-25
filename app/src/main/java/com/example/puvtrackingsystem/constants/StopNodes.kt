package com.example.puvtrackingsystem.constants

import com.example.puvtrackingsystem.classes.StopNode

fun getStopNodes(): HashMap<String, StopNode> {
    return hashMapOf(
        "Ambiong" to StopNode(16.427963835462258, 120.60737837946678),
        "Dona Manuela School" to StopNode(16.424986676282977, 120.6053877325859),
        "Police Station Crossing" to StopNode(16.426246716296536, 120.60654339150264),
        "Bayan Park (Brookspoint)" to StopNode(16.42738349336682, 120.60815423443367),
        "Crossing - Leonila Hill" to StopNode(16.423228548620695, 120.60244954242647),
        "Crossing - Jacob Store" to StopNode(16.424721071198014, 120.60635003958713),
        "Crossing - Ledesma" to StopNode(16.42435117211904, 120.6056745316049),
        "Crossing - Malvar Waiting Shed" to StopNode(16.424025651183086, 120.60441956227056),
        "Crossing - Bugallon, Floresca" to StopNode(16.423836380391656, 120.60398768872129),
        "Crossing - Rimando" to StopNode(16.423353922130303, 120.60283333216881),
        "Crossing - Upper Brookside" to StopNode(16.422260136576046, 120.60079191629916),
        "Crossing - 7/11 Rimando" to StopNode(16.42147226937073, 120.59994437535093),
        "Crossing - Church of Christ" to StopNode(16.42073164083369, 120.59933438789305),
        "SLU" to StopNode(16.419289898291982, 120.59713388753114),
        "Baguio Market" to StopNode(16.423477835961055, 120.5931614209399),
        "Igorot Garden" to StopNode(16.413328415602635, 120.59477601376031),
        "Jadewell" to StopNode(16.41264541866174, 120.59549138282917),
        "SM - Parking" to StopNode(16.408773630744776, 120.59825567081572),
        "SM - Main" to StopNode(16.4089740109058, 120.59997217194783),
        "UC Overpass" to StopNode(16.407807928851266, 120.59798539063031),
        "Patriotic" to StopNode(16.411394005391358, 120.59693782480694),
        "Tiongsan" to StopNode(16.41337574854675, 120.59526819443082),
        "Malcolm Square" to StopNode(16.414084574535284, 120.59551448136295),
        "Centermall" to StopNode(16.416273509867562, 120.59609943443337)
    )
}