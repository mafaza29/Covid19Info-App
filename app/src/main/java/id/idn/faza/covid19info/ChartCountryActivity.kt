package id.idn.faza.covid19info

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import id.idn.faza.covid19info.databinding.ActivityChartCountryBinding
import id.idn.faza.covid19info.pojo.CountriesItem
import id.idn.faza.covid19info.pojo.ResponseCountry
import id.idn.faza.covid19info.retrofit.CovidInterface
import id.idn.faza.covid19info.retrofit.RetrofitService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChartCountryActivity : AppCompatActivity() {
    //buat data yang akan diterima dari main activity
    private lateinit var dataCountry: CountriesItem

    //buat variable binding untuk view binding
    private lateinit var binding: ActivityChartCountryBinding

    //buat variable untuk menyimpan nama sumbu X
    private val dayCases = mutableListOf<String>()

    //buat Variable untuk menyimpan Data kematian, sembuh, aktif, dan terkonfirmasi
    private val dataConfirmed = mutableListOf<BarEntry>()
    private val dataDeath = mutableListOf<BarEntry>()
    private val dataRecovered = mutableListOf<BarEntry>()
    private val dataActive = mutableListOf<BarEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //deklarasi inflater dan binding
        val inflater = layoutInflater
        binding = ActivityChartCountryBinding.inflate(inflater)

        //ubah setContentView menggunakan binding.root
        setContentView(binding.root)

        //dapatkan data country dari intent parcelable
        dataCountry = intent.getParcelableExtra("DATA_COUNTRY") as CountriesItem

        //memasukan data country ke dalam viewBinding
        binding.run {
            txtNewConfirmedCurrent.text = dataCountry.newConfirmed.toString()
            txtNewDeathsCurrent.text = dataCountry.newDeaths.toString()
            txtNewRecoveredCurrent.text = dataCountry.newRecovered.toString()
            txtTotalConfirmedCurrent.text = dataCountry.totalConfirmed.toString()
            txtTotalDeathsCurrent.text = dataCountry.totalDeaths.toString()
            txtNewRecoveredCurrent.text = dataCountry.totalRecovered.toString()
            txtCurrent.text = dataCountry.countryCode
            txtCountryChart.text = dataCountry.country

            //tambahkan glide untuk menambah gambar bendera
            Glide.with(root)
                .load("https://www.countryflags.io/${dataCountry.countryCode}/flat/16.png")
                .into(imgFlagChart) // into diisi oleh imageView tujuan
        }

        //setelah membuat fungsi getCountryData, maka panggil fungsi tersebut di onCreate
        //cek dulu apakah slug country ada, jika ada baru deh panggil fungsi
        dataCountry.slug?.let { slug ->
            getCountry(slug)
        }
    }

    //buat fungsi getCountryData untuk Mendapatkan data Covid 19 berdasarkan nama negara
    private fun getCountry(countryName: String) {
        //panggil retrofit interface (covid interface)
        val retrofit = RetrofitService.buildService(CovidInterface::class.java)

        //membuat Variable format tanggal dari JSON
        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'", Locale.getDefault())
        //membuat variable format output tanggal yang bisa dimengerti manusia
        val outputDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        //buat android Coroutines
        lifecycleScope.launch {
            //buat variable countryData yang berisi dataCovid sesuai nama negara
            val countryData = retrofit.getCountryData(countryName)
            //jika data sukses diambil oleh retrofit
            if (countryData.isSuccessful) {
                //buat variable berisi data tersebut
                val dataCovid = countryData.body() as List<ResponseCountry>

                //lakukan perulangan item dari data Covid
                dataCovid.forEachIndexed { index, responseCountry ->
                    val barConfirmed =
                        BarEntry(index.toFloat(), responseCountry.Confirmed?.toFloat() ?: 0f)
                    val barDeath =
                        BarEntry(index.toFloat(), responseCountry.Deaths?.toFloat() ?: 0f)
                    val barRecovered =
                        BarEntry(index.toFloat(), responseCountry.Recovered?.toFloat() ?: 0f)
                    val barActive =
                        BarEntry(index.toFloat(), responseCountry.Active?.toFloat() ?: 0f)

                    //tambahkan data bar di atas ke dalam dataConfirmed dll
                    dataConfirmed.add(barConfirmed)
                    dataDeath.add(barDeath)
                    dataRecovered.add(barRecovered)
                    dataActive.add(barActive)

                    //jika ada tanggal / date item
                    responseCountry.Date?.let { itemDate ->
                        //parse tanggal dan ubah ke bentuk yang telah diformat sesuai output
                        val date = inputDateFormat.parse(itemDate)
                        val formattedDate = outputDateFormat.format(date as Date)
                        // tambahkan tangggal yang telah diformat sesuai format output
                        dayCases.add(formattedDate)
                    }
                }

                binding.chartView.axisLeft.axisMinimum = 0f
                val labelSumbuX = binding.chartView.xAxis
                labelSumbuX.run {
                    valueFormatter = IndexAxisValueFormatter(dayCases)
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setCenterAxisLabels(true)
                    isGranularityEnabled = (true)
                }

                //adanya keterangan warna dan legenda
                val barDataConfirmed = BarDataSet(dataConfirmed, "Confirmed")
                val barDataRecovered = BarDataSet(dataRecovered, "Recovered")
                val barDataDeath = BarDataSet(dataDeath, "Death")
                val barDataActive = BarDataSet(dataActive, "Active")

                barDataConfirmed.setColors(Color.parseColor("#F44336"))
                barDataRecovered.setColors(Color.parseColor("#FFEB3B"))
                barDataDeath.setColors(Color.parseColor("#03DAC5"))
                barDataActive.setColors(Color.parseColor("#2196F3"))

                // membuat variable data berisi semua bar data
                val dataChart =
                    BarData(barDataConfirmed, barDataRecovered, barDataDeath, barDataActive)

                //buat variable berisi spasi
                val barSpace = 0.02f
                val groupSpace = 0.3f
                val groupCount = 4f

                //modifikasi chartview programmatically
                binding.chartView.run {
                    // Tambahkan dataChart kedalam ChartView
                    data = dataChart
                    // invalidate untuk mengganti data sebelumnya (jika ada) dengan data yang baru
                    invalidate()
                    setNoDataTextColor(R.color.dkgrey)
                    //chartview bisa ditap atau di zoom
                    setTouchEnabled(true)
                    description.isEnabled = (false)
                    xAxis.axisMinimum = 0f
                    setVisibleXRangeMaximum(
                        0f + barData.getGroupWidth(
                            groupSpace,
                            barSpace
                        ) * groupCount
                    )
                    groupBars(0f, groupSpace, barSpace)
                }
            }
        }
    }
}