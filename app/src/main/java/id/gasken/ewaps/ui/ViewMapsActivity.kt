package id.gasken.ewaps.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import id.gasken.ewaps.R
import id.gasken.ewaps.data.Points
import id.gasken.ewaps.databinding.ActivityViewMapsBinding
import id.gasken.ewaps.tool.Const
import id.gasken.ewaps.tool.viewBinding

class ViewMapsActivity :
    AppCompatActivity(),
    OnMapReadyCallback {
    private val binding: ActivityViewMapsBinding by viewBinding()

    private lateinit var mMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()
    private val data: MutableList<Points> = mutableListOf()
    private lateinit var clusterManager: ClusterManager<PointItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db.collection(Const.DB_POINTS).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val point = Points()
                    point.title = document.getString(Const.TITLE)!!
                    point.position = document.getGeoPoint(Const.POSITION)?.let { LatLng(it.latitude, it.longitude) }!!
                    point.note = document.getString(Const.NOTE)!!
                    point.lastUpdate = document.getTimestamp(Const.LASTUPDATE)!!
                    data.add(point)
                }

                val mapFragment = supportFragmentManager.findFragmentById(R.id.mapview) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
            .addOnFailureListener {
                Log.e("MAP", "Error occurred, cause ${it.message}")
            }

        binding.showSearch.setOnClickListener {
            if (it.tag == "show") {
                it.tag = "hide"
                binding.showSearch.setImageResource(R.drawable.ic_baseline_close_24)
                binding.layoutSearch.visibility = View.VISIBLE
            } else {
                it.tag = "show"
                binding.showSearch.setImageResource(R.drawable.ic_search_gray)
                binding.layoutSearch.visibility = View.GONE
            }
        }

        binding.buttonSearchPoint.setOnClickListener {
            Toast.makeText(this, "Tombol Pencarian ditekan!", Toast.LENGTH_SHORT).show()
        }

        binding.buttonNavigasi.setOnClickListener {
            Toast.makeText(this, "Tombol Navigasi ditekan!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isTrafficEnabled = true
        mMap.isBuildingsEnabled = false
        mMap.isIndoorEnabled = false

        setupCluster(data)
    }

    private fun setupCluster(points: MutableList<Points>) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-7.797068, 110.370529), 15f))

        clusterManager = ClusterManager(this, mMap)

        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)

        addPoints(points)
    }

    private fun addPoints(points: MutableList<Points>) {
        for (point in points) {
            val pointItem = PointItem(
                point.title,
                point.position,
                point.note
            )
            clusterManager.addItem(pointItem)
        }
    }

    inner class PointItem(
        private val title: String,
        private val position: LatLng,
        private val snippet: String
    ) : ClusterItem {
        override fun getPosition(): LatLng = position
        override fun getTitle(): String = title
        override fun getSnippet(): String = snippet
    }
}
