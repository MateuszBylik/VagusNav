package com.comeet.vagusnav

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

class MainActivity : AppCompatActivity() {

    private var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MapLibre musi zostać zainicjowane ZANIM załadujemy widok (setContentView)
        MapLibre.getInstance(this)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)

        // Ładujemy asynchronicznie mapę
        mapView?.getMapAsync { map ->
            // Ustawiamy darmowy styl demonstracyjny (później podmienimy to na plik offline)
            map.setStyle(Style.Builder().fromUri("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json")) { style ->

                // Kiedy styl się załaduje, ustawiamy kamerę na Toruń
                val torunPosition = CameraPosition.Builder()
                    .target(LatLng(53.0138, 18.5984)) // Współrzędne centrum Torunia
                    .zoom(12.0)
                    .build()

                map.cameraPosition = torunPosition
            }
        }
    }

    // --- CYKL ŻYCIA MAPY (Bardzo ważne, żeby mapa nie "wyciekała" z RAMu) ---

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }
}