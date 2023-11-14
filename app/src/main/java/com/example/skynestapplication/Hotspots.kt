package com.example.skynestapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONException
import kotlin.concurrent.thread


class Hotspots : AppCompatActivity(), OnMapReadyCallback {

    var birdsList = mutableListOf<BirdType>()
    val LOGGING_TAG = "birdDATA"

    private var  mMap: GoogleMap? = null

    lateinit var mapView: MapView

    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

    private val DEFAULT_ZOOM = 15f

    private  var fusedLocationClient: FusedLocationProviderClient? = null

    lateinit var foundAddress: TextView

    lateinit var home: Button

    lateinit var kiloM: Button

    lateinit var miles: Button

    var end_latitude = 0.0

    var end_longitude = 0.0

    private var origin: MarkerOptions? = null

    private var destination: MarkerOptions? = null

    var latitude = 0.0

    var longitude = 0.0

    private  val PADDING = 100

    override fun onMapReady(googleMap: GoogleMap) {

       mapView.onResume()
        mMap = googleMap

        askPermissionLocation()
        thread {

            val birds = try {
                buildURLForBird()?.readText()
            } catch (e: Exception) {
                return@thread
            }

            runOnUiThread { consumeJson(birds) }
          //  Toast.makeText(this@Hotspots, "Nearby = $birds", Toast.LENGTH_SHORT).show()
        }
        //addBirdMarkers()
        if(ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap!!.isMyLocationEnabled = true

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotspots)

        mapView = findViewById<MapView>(R.id.my_map)

        foundAddress = findViewById<TextView>(R.id.tv_display)

        home = findViewById<Button>(R.id.extended_home)

        kiloM = findViewById<Button>(R.id.btn_find)

        miles = findViewById<Button>(R.id.btn_miles)

        // Fetching API_KEY which we wrapped
        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        askPermissionLocation()

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }

        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        home.setOnClickListener {
            val intent = Intent(this, HomeDashboard::class.java)
            startActivity(intent)
        }

        kiloM.setOnClickListener {
            searchAreaKM()
        }

        miles.setOnClickListener {
            searchAreaMi()
        }

    }

    fun consumeJson(birdsJSON: String?) {
        if (birdsJSON != null) {
            try {
                val birds = JSONArray(birdsJSON)
                for (i in 0 until birds.length()) {
                    val observationObject = BirdType()
                    val dailyObs = birds.getJSONObject(i)

                    val name = dailyObs.getString("comName")
                    Log.i(LOGGING_TAG, "consumeJson: Name$name")
                    observationObject.comName = name

                    val sciName = dailyObs.getString("sciName")
                    Log.i(LOGGING_TAG, "consumeJson: sciName$sciName")
                    observationObject.sciName = sciName

                    val locName = dailyObs.getString("locName")
                    Log.i(LOGGING_TAG, "consumeJson: locName$locName")
                    observationObject.locName = locName

                    val lat = dailyObs.getDouble("lat")
                    Log.i(LOGGING_TAG, "consumeJson: lat$lat")
                    observationObject.lat = lat

                    val lng = dailyObs.getDouble("lng")
                    Log.i(LOGGING_TAG, "consumeJson: lng$lng")
                    observationObject.lng = lng
                    val birdLatLng = LatLng(lat, lng)
                    birdsList.add(observationObject)
                    runOnUiThread {
                        mMap!!.addMarker(MarkerOptions().icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN)).position(birdLatLng).title(locName)
                            .snippet("Scientific Name: $sciName\nLocation: $name locName"))
                    }
                }


            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

private fun searchAreaMi() {
        val searchLocation = findViewById<View>(R.id.et_location) as EditText
        val theLocation = searchLocation.text.toString()
        var  addressList: List<Address>? = null
        val markerOptions = MarkerOptions()

        Log.d("location = ", theLocation)
        if(theLocation != "") {
            val geocoder = Geocoder(applicationContext)
            try {
                addressList = geocoder.getFromLocationName(theLocation, 5)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (addressList != null) {
                for (a in addressList.indices) {

                    val myAddress = addressList[a]
                    val latLng = LatLng(myAddress.latitude, myAddress.longitude)
                    val markOpt = MarkerOptions()
                    val results = FloatArray(10)


                    markerOptions.position(latLng)
                    mMap!!.addMarker(markerOptions)
                    end_latitude = myAddress.latitude
                    end_longitude = myAddress.longitude
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))

                    markOpt.title("Distance")
                    Location.distanceBetween(
                        latitude,
                        longitude,
                        end_latitude,
                        end_longitude,
                        results
                    )

                    val str = String.format("%.1f", results[0]/ (1000 * 0.6213721192))

                    //This sets the marker to draw route between the two points
                    origin = MarkerOptions().position(LatLng(latitude,longitude))
                        .title("You").snippet("origin")
                    destination = MarkerOptions().position(LatLng(end_latitude, end_longitude))
                        .title(searchLocation.text.toString())
                        .snippet("Distance = $str miles")
                    destination?.let { node -> mMap!!.addMarker(node) }
                    origin?.let { node1 -> mMap!!.addMarker(node1) }

                   // mMap!!.addMarker(destination)
                    //mMap!!.addMarker(origin!!)

                    Toast.makeText(this@Hotspots, "Distance = $str miles", Toast.LENGTH_SHORT).show()

                    foundAddress.text = "Distance = $str miles"

                   // val url: String = getDirectionsUrl(origin!!.position, destination!!.position)!!
                    //val downloadTask: DownloadTask = DownloadTask()
                    //downloadTask.execute(url)

                    //Get the URL to the Google Directions API
                    //origin?.let { node -> node.position }
                    //val url: String = getDirectionsUrl(origin.let { node -> node!!.position }, destination.let { node -> node!!.position })!!
                    //val url: String = getDirectionsUrl(origin!!.position, destination!!.position)!!
                   // val downloadTask: DownloadTask = DownloadTask()
                   // downloadTask.execute(url)

                    /*Log.d("GoogleMap", "before URL")
                    val url = getDirectionURL(origin!!.position,destination!!.position)
                    //val url = getDirectionsUrl(origin, destination)!!
                    Log.d("GoogleMap", "URL : $url")
                     val downloadTask: DownloadTask = DownloadTask()
                     downloadTask.execute(url)
                    GetDirection(url).execute()*/
                   // val url: String = getDirectionsUrl(origin.let { node -> node!!.position }, destination.let { node -> node!!.position })!!
                    //GetDirection(url).execute()


                }
            }
        }
    }

    private fun searchAreaKM() {
        val searchLocation = findViewById<View>(R.id.et_location) as EditText
        val theLocation = searchLocation.text.toString()
        var  addressList: List<Address>? = null
        val markerOptions = MarkerOptions()

        Log.d("location = ", theLocation)
        if(theLocation != "") {
            val geocoder = Geocoder(applicationContext)
            try {
                addressList = geocoder.getFromLocationName(theLocation, 5)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (addressList != null) {
                for (a in addressList.indices) {

                    val myAddress = addressList[a]
                    val latLng = LatLng(myAddress.latitude, myAddress.longitude)
                    val markOpt = MarkerOptions()
                    val results = FloatArray(10)


                    markerOptions.position(latLng)
                    mMap!!.addMarker(markerOptions)
                    end_latitude = myAddress.latitude
                    end_longitude = myAddress.longitude
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))

                    markOpt.title("Distance")
                    Location.distanceBetween(
                        latitude,
                        longitude,
                        end_latitude,
                        end_longitude,
                        results
                    )

                    val str = String.format("%.1f", results[0]/ 1000)

                    //This sets the marker to draw route between the two points
                    origin = MarkerOptions().position(LatLng(latitude,longitude))
                        .title("You").snippet("origin")
                    destination = MarkerOptions().position(LatLng(end_latitude, end_longitude))
                        .title(searchLocation.text.toString())
                        .snippet("Distance = $str KM")


                    destination?.let { node -> mMap!!.addMarker(node) }
                    origin?.let { node -> mMap!!.addMarker(node) }

                    //mMap!!.addMarker(destination)
                    //mMap!!.addMarker(origin ?)

                    Toast.makeText(this@Hotspots, "Distance = $str KM", Toast.LENGTH_SHORT).show()

                    foundAddress.text = "Distance = $str KM"



                    val startLocation = LatLng(latitude,longitude)
                    val endLocation = LatLng(end_latitude,end_longitude)
                    val url: String = getDirectionsUrl(startLocation,endLocation)
                    //val url: String = getDirectionsUrl(origin!!.position, destination!!.position)
                    val downloadTask: DownloadTask = DownloadTask()
                   // downloadTask.execute(url)
                    GetDirection(url).execute()
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 14F))

                    // Calculate and display the route between the current location and searched location
                    // Get the searchedLocation LatLng object (e.g., from user input or from a search result)
                    val polylineOptions = PolylineOptions()
                        .add(startLocation)  // Start point
                        .add(endLocation)  // End point
                        .width(8f)
                        .color(Color.BLUE)
                    polylineOptions.geodesic(true)

                    mMap!!.addPolyline(polylineOptions)

                    // Set the bounds to include both locations
                    val bounds = LatLngBounds.Builder()
                        .include(startLocation)
                        .include(endLocation)
                        .build()
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, PADDING))
                }
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private inner class DownloadTask :AsyncTask<String?, Void?, String>() {
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            val parserTask = ParserTask()
            parserTask.execute(result)
        }

        override fun doInBackground(vararg url: String?): String {
            var data = ""
            try {
                data = downloadUrl(url[0].toString())
            } catch (e: java.lang.Exception) {
                Log.d("Background Task", e.toString())
            }
            return data
        }
    }
//----------------------------------------------------------------------------------------------------
    /**
     * A class to parse the JSON format
     */
    @SuppressLint("StaticFieldLeak")
    private inner class ParserTask: AsyncTask<String?, Int?, List<List<HashMap<String, String>>>?>() {
        //Parsing the data in non-ui thread
        override fun doInBackground(vararg jsonData: String?): List<List<HashMap<String, String>>>? {
            val jObject: JSONObject
            var  routes: List<List<HashMap<String, String>>>? = null
            try {
                jObject = JSONObject(jsonData[0])
                val parser = DataParser()
                routes = parser.parse(jObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return routes
        }

        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            val points = ArrayList<LatLng?>()
            val lineOptions = PolylineOptions()
            for (i in result!!.indices) {
                val path = result[i]
                for (j in path.indices) {
                    val point = path[j]
                    val lat = point["lat"]!!.toDouble()
                    val lng = point["lng"]!!.toDouble()
                    val position = LatLng(lat, lng)
                    points.add(position)
                }
                lineOptions.addAll(points)
                lineOptions.width(8f)
                lineOptions.color(Color.GREEN)
                lineOptions.geodesic(true)
            }

            //Drawing polyline in the Google Map for the i-th route
                mMap!!.addPolyline(lineOptions)


        }
    }
    //--------------------------------------------------------------------------------------------------------------------
    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {
        //Origin of the route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        //Origin of the route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        //Sensor enabled
        val  sensor = "sensor=false"

        //Setting the transportation mode
        val  mode = "mode=driving"

        //Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$mode"

        //The output format
        val output = "json"

        //Building the url to the web service
        return  "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyAkZWraPfa9He5onUjmZlmXW_yzaeIfA20"

    }

//--------------------------------------------------------------------------------------------------
    /**
     * A method to download json data from url
     */
    @Throws(IOException::class)
    private  fun downloadUrl(strUrl: String): String {
        var  data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()
            iStream = urlConnection.inputStream
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            var line: String? = ""
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            data = sb.toString()
            br.close()
        } catch (e: java.lang.Exception) {
            Log.d("Exception", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return  data
    }
//--------------------------------------------------------------------------------------------------------------------
    fun getDirectionURL(origin:LatLng,dest:LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyAkZWraPfa9He5onUjmZlmXW_yzaeIfA20"
    }

    //--------------------------------------------------------------------------------------------------------------------
    inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result

        }

        override fun onPostExecute(result: List<List<LatLng>>?) {
            val lineoption = PolylineOptions()
            for (i in result!!.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.GREEN)
                lineoption.geodesic(true)
            }
            mMap!!.addPolyline(lineoption)
        }

    }


    //--------------------------------------------------------------------------------------------------------------------
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        askPermissionLocation()

        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }

    //--------------------------------------------------------------------------------------------------------------------
    private fun askPermissionLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        getCurrentLocation()

    }

    //--------------------------------------------------------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@Hotspots)

        try{
            val location =  fusedLocationClient!!.lastLocation

            location.addOnCompleteListener(object : OnCompleteListener<Location> {
                override fun onComplete(loc: Task<Location>) {
                    if (loc.isSuccessful) {

                        val currentLocation = loc.result
                        if (currentLocation != null) {
                            moveCamera(
                                LatLng(currentLocation.latitude, currentLocation.longitude),
                                DEFAULT_ZOOM
                            )

                            latitude = currentLocation.latitude
                            longitude = currentLocation.longitude

                        }
                    } else {
                        askPermissionLocation()

                    }
                }
            })
        } catch (se: Exception) {
            Log.e("TAG", "Security Exception")
        }

    }
    //--------------------------------------------------------------------------------------------------------------------
    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("$currentLatLong")
        mMap!!.addMarker((markerOptions))

    }

    //--------------------------------------------------------------------------------------------------------------------
    private fun moveCamera(latLng: LatLng, zoom: Float) {
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }
    //--------------------------------------------------------------------------------------------------------------------
    fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

}
//--------------------------------------------------------------------------------------------------