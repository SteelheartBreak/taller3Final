package com.example.taller3.model


class LocationG {

    var latitud: Double = 0.0
    var longitude: Double = 0.0
    lateinit var name: String

    constructor(
        latitud: Double,
        longitude: Double,
        name: String
    ) {
        this.latitud = latitud
        this.longitude = longitude
        this.name = name

    }

    constructor(){}

    override fun toString(): String {
        return "$name"
    }

}