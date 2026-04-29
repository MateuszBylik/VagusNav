package com.comeet.vagusnav

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.location.permissions.PermissionsListener
import org.maplibre.android.location.permissions.PermissionsManager
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

class MainActivity : AppCompatActivity(), PermissionsListener {

    private var mapView: MapView? = null
    private var mapLibreMap: MapLibreMap? = null
    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)

        val fabLocation = findViewById<FloatingActionButton>(R.id.fabLocation)
        val compassView = findViewById<ImageView>(R.id.compassView)

        // Ładujemy asynchronicznie mapę
        mapView?.getMapAsync { map ->
            this.mapLibreMap = map
            val uiSettings = map.uiSettings

            uiSettings.isLogoEnabled = false
            uiSettings.isAttributionEnabled = false
            uiSettings.isCompassEnabled = false

            // Ustawiamy początkową pozycję (Bydgoszcz/Toruń) przed włączeniem czegokolwiek
            val initialPosition = CameraPosition.Builder()
                .target(LatLng(53.0138, 18.5984))
                .zoom(12.0)
                .build()
            map.moveCamera(CameraUpdateFactory.newCameraPosition(initialPosition))

            map.addOnCameraMoveListener {
                compassView.rotation = -map.cameraPosition.bearing.toFloat()
            }

            compassView.setOnClickListener {
                val currentBearing = map.cameraPosition.bearing
                val isFacingNorth = currentBearing <= 1.0 || currentBearing >= 359.0

                // WYŁĄCZAMY automatyczne śledzenie kamery, żeby móc obrócić mapę bez jej przesuwania
                if (map.locationComponent.isLocationComponentActivated) {
                    map.locationComponent.cameraMode = CameraMode.NONE
                }

                // 1. Jeśli lokalizacja jest wyłączona
                if (!PermissionsManager.areLocationPermissionsGranted(this) || !isSystemLocationEnabled()) {
                    Toast.makeText(this, "Włącz lokalizację", Toast.LENGTH_SHORT).show()
                    if (!isFacingNorth) {
                        resetToNorth(map)
                    }
                    return@setOnClickListener
                }

                // 2. Jeśli lokalizacja działa
                if (isFacingNorth) {
                    // Jesteśmy na północy -> włączamy śledzenie kierunku (to wycentruje mapę na użytkowniku)
                    map.locationComponent.cameraMode = CameraMode.TRACKING_COMPASS
                    Toast.makeText(this, "Śledzenie kierunku włączone", Toast.LENGTH_SHORT).show()
                } else {
                    // Mapa jest obrócona -> tylko "północujemy" bez przesuwania środka
                    resetToNorth(map)
                }
            }

            map.setStyle(Style.Builder().fromUri("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json")) { style ->
                // Gdy styl się załaduje, próbujemy włączyć lokalizację
                enableLocationComponent(style)
            }
        }

        fabLocation.setOnClickListener {
            if (!isSystemLocationEnabled()) {
                Toast.makeText(this, "Włącz lokalizację", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!PermissionsManager.areLocationPermissionsGranted(this)) {
                permissionsManager.requestLocationPermissions(this)
                return@setOnClickListener
            }

            mapLibreMap?.let { map ->
                if (map.locationComponent.isLocationComponentActivated) {
                    // Wyśrodkuj na mnie, ale NIE OBRAZAJ mapy
                    map.locationComponent.cameraMode = CameraMode.TRACKING
                } else {
                    map.style?.let { style -> enableLocationComponent(style) }
                }
            }
        }
    }

    // --- PÓŁNOCOWANIE KAMERY (Animacja) ---
    private fun resetToNorth(map: MapLibreMap) {
        val currentPosition = map.cameraPosition
        val position = CameraPosition.Builder()
            .target(currentPosition.target)
            .zoom(currentPosition.zoom)
            .bearing(0.0)
            .build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500)
    }

    // --- WERYFIKACJA STANU GPS ---
    private fun isSystemLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this) && isSystemLocationEnabled()) {
            val locationComponent = mapLibreMap?.locationComponent
            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, loadedMapStyle).build()

            locationComponent?.activateLocationComponent(locationComponentActivationOptions)
            locationComponent?.isLocationComponentEnabled = true

            // UWAGA: Nie ustawiamy od razu CameraMode.TRACKING, żeby uniknąć skoku na (0,0) przed fixem GPS.
            // Ustawiamy NONE, co pozwoli mapie zostać na domyślnych koordynatach do czasu kliknięcia FAB lub fixa.
            locationComponent?.cameraMode = CameraMode.NONE
            locationComponent?.renderMode = RenderMode.COMPASS
            
            // Próbujemy wycentrować tylko jeśli mamy już jakąś znaną lokalizację
            locationComponent?.lastKnownLocation?.let {
                val position = CameraPosition.Builder()
                    .target(LatLng(it.latitude, it.longitude))
                    .zoom(12.0)
                    .build()
                mapLibreMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                locationComponent.cameraMode = CameraMode.TRACKING
            }
        } else {
            // Jeśli brak uprawnień/GPS - mapa zostaje na domyślnych (ustawionych w getMapAsync)
            if (!PermissionsManager.areLocationPermissionsGranted(this)) {
                permissionsManager = PermissionsManager(this)
                permissionsManager.requestLocationPermissions(this)
            }
        }
    }

    // Wymagane metody PermissionsListener
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "Aplikacja potrzebuje lokalizacji do nawigacji.", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            mapLibreMap?.style?.let { style -> enableLocationComponent(style) }
        } else {
            Toast.makeText(this, "Brak uprawnień do lokalizacji", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() { super.onStart(); mapView?.onStart() }
    override fun onResume() { super.onResume(); mapView?.onResume() }
    override fun onPause() { super.onPause(); mapView?.onPause() }
    override fun onStop() { super.onStop(); mapView?.onStop() }
    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); mapView?.onSaveInstanceState(outState) }
    override fun onLowMemory() { super.onLowMemory(); mapView?.onLowMemory() }
    override fun onDestroy() { super.onDestroy(); mapView?.onDestroy() }
}