package kr.co.huve.wealthApp.util.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class DoubleDeserializer : JsonDeserializer<Double> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Double {
        if (json != null && context != null && json.isJsonPrimitive) {
            val item = json.asString.toDoubleOrNull()
            if (item != null) {
                return item
            }
        }
        return -1.0
    }
}