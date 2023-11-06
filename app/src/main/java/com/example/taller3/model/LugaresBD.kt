package com.example.taller3.model

val lugaresBD = mutableListOf<LocationG>()

// metodo que retorna una lista de lugares ficticios. es la "Base de Datos"
fun getLugares(): List<LocationG> {

    lugaresBD.add(
        LocationG(4.660557,
            -74.090749,
            "Parque Simón Bolivar"
        )
    )

    lugaresBD.add(
        LocationG(
            4.628308,
            -74.064929,
            "Pontificia Universidad Javeriana"
        )
    )

    lugaresBD.add(
        LocationG(
            4.596862,
            -74.072810,
            "Biblioteca Luis Angel Arango"
        )
    )

    lugaresBD.add(
        LocationG(
            4.651711,
            -74.055819,
            "Zona Gastronómica de Bogotá"
        )
    )

    lugaresBD.add(
        LocationG(
            4.695177,
            -74.030930,
            "Usaquen"
        )
    )

    return lugaresBD
}

fun getLugarName(name: String): LocationG {
    for (lugar in lugaresBD) {
        if (lugar.name == name) {
            return lugar
        }
    }
    return LocationG()

}
