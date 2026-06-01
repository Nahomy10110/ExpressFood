package cr.una.expressfood.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromIngredientsList(list: List<String>?): String =
        list?.joinToString("||") ?: ""

    @TypeConverter
    fun toIngredientsList(data: String?): List<String> =
        if (data.isNullOrBlank()) emptyList() else data.split("||")
}