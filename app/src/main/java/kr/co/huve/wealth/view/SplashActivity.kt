package kr.co.huve.wealth.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.huve.wealth.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

//        val sampleXml = ("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
//                + "<mobilegate>"
//                + "<timestamp>232423423423</timestamp>"
//                + "<txn>" + "Transaction" + "</txn>"
//                + "<amt>" + 0 + "</amt>"
//                + "</mobilegate>")
//        var jsonObj: JSONObject? = null
//        try {
//            jsonObj = XML.toJSONObject(sampleXml)
//        } catch (e: JSONException) {
//            Log.e("JSON exception", "" + e.message)
//            e.printStackTrace()
//        }
//        Log.d("XML", sampleXml)
//        Log.d("JSON", jsonObj.toString())
    }
}