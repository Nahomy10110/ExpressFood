package cr.una.expressfood.data.repository

import android.content.Context
import android.net.Uri
import cr.una.expressfood.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class CloudinaryRepository(private val context: Context) {

    /**
     * Sube una imagen desde la URI de la galería a Cloudinary.
     * Retorna la URL segura de la imagen subida.
     */
    suspend fun uploadImage(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw Exception("No se pudo abrir la imagen")

            val imageBytes = inputStream.readBytes()
            inputStream.close()

            val boundary  = "----FormBoundary${UUID.randomUUID()}"
            val url       = URL(Constants.Cloudinary.UPLOAD_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod     = "POST"
                doOutput          = true
                doInput           = true
                useCaches         = false
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }

            DataOutputStream(connection.outputStream).use { dos ->
                // Campo upload_preset
                dos.writeBytes("--$boundary\r\n")
                dos.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
                dos.writeBytes("${Constants.Cloudinary.UPLOAD_PRESET}\r\n")

                // Campo api_key
                dos.writeBytes("--$boundary\r\n")
                dos.writeBytes("Content-Disposition: form-data; name=\"api_key\"\r\n\r\n")
                dos.writeBytes("${Constants.Cloudinary.API_KEY}\r\n")

                // Campo folder
                dos.writeBytes("--$boundary\r\n")
                dos.writeBytes("Content-Disposition: form-data; name=\"folder\"\r\n\r\n")
                dos.writeBytes("products\r\n")

                // Archivo de imagen
                dos.writeBytes("--$boundary\r\n")
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"product_${System.currentTimeMillis()}.jpg\"\r\n")
                dos.writeBytes("Content-Type: image/jpeg\r\n\r\n")
                dos.write(imageBytes)
                dos.writeBytes("\r\n")

                // Cierre
                dos.writeBytes("--$boundary--\r\n")
                dos.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "Error desconocido"
                throw Exception("Cloudinary error $responseCode: $error")
            }

            val response = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            val json = JSONObject(response)
            json.getString("secure_url")
        }
    }

    companion object {
        fun default(context: Context) = CloudinaryRepository(context)
    }
}