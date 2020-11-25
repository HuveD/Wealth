package kr.co.huve.wealthApp.util.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import kr.co.huve.wealthApp.util.repository.network.data.CovidItem
import kr.co.huve.wealthApp.util.repository.network.data.CovidItems
import java.lang.reflect.Type

class CovidItemDeserializer : JsonDeserializer<CovidItems> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): CovidItems {
        if (json != null && context != null && json.isJsonObject) {
            val item = json.asJsonObject["item"]
            val dataSet = arrayListOf<CovidItem>()
            if (item.isJsonArray) {
                val jsonArray = item.asJsonArray
                for (i in 0 until item.asJsonArray.size()) {
                    val data = context.deserialize<CovidItem>(jsonArray[i], CovidItem::class.java)
                    dataSet.add(data)
                }
            }
            return CovidItems(dataSet)
        }
        return CovidItems(emptyList())
    }
}