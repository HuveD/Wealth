package kr.co.huve.wealth.util.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import kr.co.huve.wealth.model.backend.data.Item
import kr.co.huve.wealth.model.backend.data.Items
import java.lang.reflect.Type

class CovidItemDeserializer : JsonDeserializer<Items> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Items {
        if (json != null && context != null && json.isJsonObject) {
            val item = json.asJsonObject["item"]
            val dataSet = arrayListOf<Item>()
            if (item.isJsonArray) {
                val jsonArray = item.asJsonArray
                for (i in 0 until item.asJsonArray.size()) {
                    val data = context.deserialize<Item>(jsonArray[i], Item::class.java)
                    dataSet.add(data)
                }
            }
            return Items(dataSet)
        }
        return Items(emptyList())
    }
}