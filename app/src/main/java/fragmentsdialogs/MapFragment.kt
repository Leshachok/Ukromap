package fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import architecture.viewmodels.MainViewModel
import com.example.ukromap.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.maps.android.clustering.ClusterManager
import mapobjects.*
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(), OnMapReadyCallback {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var locationPermissionGranted = false
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mainViewModel: MainViewModel
    private var timer:Timer = Timer()
    private var isGpsInfoShown = false
    private lateinit var clusterManager: ClusterManager<SightMarker>
    private var REASON_GESTURE = 1
    private lateinit var googleSignInClient:GoogleSignInClient

    companion object {
        var locationPermissionGranted = false
        var id:Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        checkPermissions()
        getGoogleClient()

        Companion.id = id

        initViewModel()
        fusedLocationProviderClient = context?.let {
            LocationServices.getFusedLocationProviderClient(it)
        }!!

    }

    private fun initViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(MainViewModel::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
    }


    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)

        try {
            MapsInitializer.initialize(requireActivity().applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mapView.getMapAsync(this)

        mapView.onResume()

        return view
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(googleM: GoogleMap?) {
        if (googleM != null) googleMap = googleM

        getDeviceLocation()

        setUpMap()

        setMapBordersAndStyles()

        googleMap.setOnMyLocationChangeListener { getDeviceLocation() }

        updateLocationUI()

        RegionManager.createPolygons(googleMap)

        clusterManager = ClusterManager(context, googleMap)
        var renderer = MarkerRenderer(requireContext(), googleMap, clusterManager)
        renderer.minClusterSize = 1
        clusterManager.renderer = renderer
        googleMap.setOnMarkerClickListener(clusterManager)
        googleMap.setOnCameraIdleListener(clusterManager)


        clusterManager.setOnClusterItemClickListener { item -> false }
        clusterManager.setOnClusterClickListener { cluster ->
            var builder = LatLngBounds.builder()
            cluster.items.forEach { item-> builder.include(
                LatLng(
                    item.position.latitude,
                    item.position.longitude
                )
            ) }
            var bounds = builder.build()
            try {
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds, 400
                    )
                )
            } catch (e: java.lang.Exception) {
                Log.e("TAG_SNAP", "onClusterClick: " + e.message)
            }
            true
        }
        clusterManager.markerCollection.setOnInfoWindowAdapter(
            MarkerInfoWindow(
                layoutInflater,
                requireContext()
            )
        )
        googleMap.setInfoWindowAdapter(clusterManager.markerManager)
        googleMap.setOnInfoWindowClickListener { marker ->
            var route = "http://maps.google.com/maps?daddr=" + marker.position.latitude.toString() + ',' + marker.position.longitude.toString()
            var intent = Intent(
                Intent.ACTION_VIEW, Uri.parse(route)
            )
            context?.startActivity(intent)
        }

        googleMap.setOnCameraMoveStartedListener { reason->
            if(reason == REASON_GESTURE)
                clusterManager.markerCollection.markers.forEach { m-> m.hideInfoWindow() }
        }
        SightManager.renderSights(googleMap, clusterManager)

        moveCamera()

    }

    private fun setUpMap() {
        val mUiSettings: UiSettings = googleMap.uiSettings
        mUiSettings.isCompassEnabled = true
        mUiSettings.isScrollGesturesEnabled = true
        mUiSettings.isZoomGesturesEnabled = true
        mUiSettings.isTiltGesturesEnabled = true
        mUiSettings.isRotateGesturesEnabled = true
    }

    private fun setMapBordersAndStyles() {
        googleMap.setMinZoomPreference(5f)
        val ukraineBounds = LatLngBounds(
            LatLng((42.572038), 22.800609),  // SW bounds
            LatLng((52.342441), 39.620143) // NE bounds
        )
        googleMap.setLatLngBoundsForCameraTarget(ukraineBounds)
        var mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
            requireContext(),
            R.raw.googlemapstyle
        )
        googleMap.setMapStyle(mapStyleOptions)
    }

    private fun moveCamera() {
        val ukraine = LatLng(49.852, 31.211)
        val camera = CameraPosition.Builder().target(ukraine).zoom(4.5f).build()
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera))
    }

    private fun checkPermissions(){
        var permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        locationPermissionGranted = permission == PackageManager.PERMISSION_GRANTED

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("permission", "fragment")
        Log.d("permisson1", "fragment + ${Companion.locationPermissionGranted}")
        when(requestCode){
            1 -> {
                locationPermissionGranted = (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Log.d("permisson2", "fragment + ${Companion.locationPermissionGranted}")
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI(){
        try {
            if(locationPermissionGranted){
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = true
                getDeviceLocation()
            }else{
                googleMap.isMyLocationEnabled = false
                googleMap.uiSettings.isMyLocationButtonEnabled = false
            }
        }catch (e: SecurityException){
            e.message?.let { Log.e("Exception: %s", it) }
        }
    }

    private fun getGoogleClient() {
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

    }

    private fun silentSignIn(callback: (String) -> Unit){
        var preferences = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE)
        googleSignInClient.silentSignIn()
            .addOnCompleteListener { task -> run{
                var token = task.result.idToken ?: ""
                var oldtoken = preferences.getString("idToken", "")
                if(token != oldtoken){
                    preferences.edit().putString("idToken", token).apply()
                    Log.d("token", "idtoken changed $token")
                }
                thread {
                    callback(token)
                }
            }  }
    }


    @SuppressLint("SetTextI18n", "MissingPermission")
    private fun getDeviceLocation(){

        if(locationPermissionGranted){
            var locationResult:Task<Location> = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener{ task: Task<Location> ->
                if(task.isSuccessful){
                    if(task.result != null){
                        lastLocation = task.result
                        var mypos = LatLng(lastLocation.latitude, lastLocation.longitude)

                        var userPosition = "невідомо"

                        SightManager.listSights.forEach { sight->
                            var point = sight.circle.center
                            var ky = 40000 / 360
                            var kx = cos(Math.PI * point.latitude / 180.0) * ky
                            var dx = abs(point.longitude - mypos.longitude) * kx
                            var dy = abs(point.latitude - mypos.latitude) * ky
                            if(sqrt(dx * dx + dy * dy) <= sight.circle.radius/1000.0){
                                userPosition = sight.name
                                var set = mainViewModel.getVisitedSights()
                                if(!set!!.contains(sight.guid)){
                                    //TODO диалог
                                    silentSignIn { _ ->
                                        mainViewModel.addVisitedSight(sight.guid)
                                    }
                                }
                            }
                        }
                    }
                    else if (!isGpsInfoShown){
                        Toast.makeText(context, "GPS вимкнуто", Toast.LENGTH_LONG).show()
                        isGpsInfoShown = true
                    }
                }else{
                    Log.d(TAG, "Current location is null. Using defaults.")
                    googleMap.uiSettings.isMyLocationButtonEnabled = false
                }
            }
        }
    }

}