package kr.co.huve.wealthApp.model.repository.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class IntDeserializer : JsonDeserializer<Int> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Int {
        if (json != null && context != null && json.isJsonPrimitive) {
            val item = json.asString.toIntOrNull()
            if (item != null) {
                return item
            }
        }
        return -1
    }
}