package com.example.puvtrackingsystem.classes

object Map {
    val routes: Array<StopNode> = arrayOf(
        StopNode("Evangelista", 16.42730419726418, 120.60562532421416),
        StopNode("Police Station", 16.426246716296536, 120.60654339150264),
        StopNode("Eskwelahan", 16.424986676282977, 120.6053877325859),
        StopNode("Gen De Jesus Crossing", 16.42435117211904, 120.6056745316049),
        StopNode("Malvar Crossing", 16.424175159595148, 120.6046161943846),
        StopNode("Floresca Crossing", 16.423836380391656, 120.60398768872129),
        StopNode("Guevarra Crossing", 16.423457740316355, 120.60302938643773),
        StopNode("Upper Brookside", 16.422260136576046, 120.60079191629916),
        StopNode("7/11", 16.42147226937073, 120.59994437535093),
        StopNode("New Lucban", 16.42073164083369, 120.59933438789305),
        StopNode("SLU", 16.419289898291982, 120.59713388753114),
        StopNode("Market", 16.41517819801149, 120.59567058248214),
        StopNode("Igorot Garden", 16.413328415602635, 120.59477601376031),
        StopNode("Jadewell", 16.41264541866174, 120.59549138282917),
        StopNode("City High", 16.407835419617587, 120.59786480716929),
        StopNode("UC SM", 16.408773630744776, 120.59825567081572),
        StopNode("SM Main", 16.4089740109058, 120.59997217194783),
        StopNode("UC Overpass", 16.407807928851266, 120.59798539063031),
        StopNode("Patriotic", 16.411394005391358, 120.59693782480694),
        StopNode("Tiongsan", 16.41337574854675, 120.59526819443082),
        StopNode("Malcolm", 16.414084574535284, 120.59551448136295),
        StopNode("Centermall", 16.416273509867562, 120.59609943443337),
        StopNode("SLU", 16.41908143598522, 120.59700999970748),
        StopNode("New Lucban", 16.42073164083369, 120.59933438789305),
        StopNode("Tulay Rimando", 16.42147226937073, 120.59994437535093),
        StopNode("Upper Brookside", 16.422260136576046, 120.60079191629916),
        StopNode("Guevarra Crossing", 16.423457740316355, 120.60302938643773),
        StopNode("Bugallon Crossing", 16.423836380391656, 120.60398768872129),
        StopNode("Malvar Crossing", 16.424175159595148, 120.6046161943846),
        StopNode("Ledesma Crossing", 16.42435117211904, 120.6056745316049 ),
        StopNode("Eskewelahan", 16.424986676282977, 120.6053877325859),
        StopNode("Police Station", 16.426246716296536, 120.60654339150264),
        StopNode("Brookspoint", 16.42738349336682, 120.60815423443367),
        StopNode("Ambiong", 16.427963835462258, 120.60737837946678),
    )

    fun getStopNames(): List<String> {
        return this.routes.map { stopNode ->
            stopNode.name
        }
    }

    fun getTownStops(start: Int = 0): List<String> {
        var idx = start

        if (idx < 0) {
            idx = 0
        } else if (idx > 16) {
            idx = 16
        }

        return this.getStopNames().slice(idx..16)
    }

    fun getHomeStops(start: Int = 17): List<String> {
        var idx = start

        if (idx < 17) {
            idx = 17
        } else if (idx > 33) {
            idx = 33
        }

        return this.getStopNames().slice(idx..33)
    }

    fun measurePathDistance(stop1: Int, stop2: Int): Double {
        var totalDistance = 0.0
        var originPointer = stop1
        val destinationPointer = stop2

        while (originPointer != destinationPointer) {
            var next = originPointer + 1

            if (next == this.routes.size) {
                next = 0
            }

            val originNode = this.routes[originPointer]
            val nextNode = this.routes[next]
            val distance = originNode.coordinates.distanceTo(nextNode.coordinates)

            totalDistance += distance

            originPointer = next
        }

        return totalDistance
    }

    fun measurePUVDistance(puv: PUV, destination: Int): Double {
        val next = puv.nextStop - 1
        val nextNode = this.routes[next]

        val distanceToNext = puv.coordinates.distanceTo(nextNode.coordinates)
        val pathDistance = this.measurePathDistance(next, destination)

        return distanceToNext + pathDistance
    }
}