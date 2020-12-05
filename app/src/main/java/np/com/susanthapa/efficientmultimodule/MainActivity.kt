package np.com.susanthapa.efficientmultimodule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import np.com.susanthapa.efficientmultimodule.databinding.ActivityMainBinding
import np.com.susanthapa.feature_a.FeatureAActivity
import np.com.susanthapa.feature_b.FeatureBActivity
import np.com.susanthapa.feature_c.FeatureCActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.featureA.setOnClickListener {
            startActivity(Intent(this, FeatureAActivity::class.java))
        }

        binding.featureB.setOnClickListener {
            startActivity(Intent(this, FeatureBActivity::class.java))
        }

        binding.featureC.setOnClickListener {
            startActivity(Intent(this, FeatureCActivity::class.java))
        }
    }
}