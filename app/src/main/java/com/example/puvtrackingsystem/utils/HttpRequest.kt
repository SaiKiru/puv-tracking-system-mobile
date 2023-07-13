import android.os.AsyncTask
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpGetRequestAsyncTask(
    private val callback: ((String) -> Unit)? = null,
    private val errorHandler: (() -> Unit)? = null
) :
    AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg params: String): String {
        val urlString = params[0]
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.readTimeout = 5000
        connection.connectTimeout = 5000

        try {
            connection.connect()

            val responseCode = connection.responseCode
            val responseBody = StringBuilder()

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStreamReader = InputStreamReader(connection.inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)

                var line: String? = bufferedReader.readLine()
                while (line != null) {
                    responseBody.append(line)
                    line = bufferedReader.readLine()
                }

                bufferedReader.close()
            }

            connection.disconnect()
            return responseBody.toString()
        } catch (e: Exception) {
            return "Error"
        }
    }

    override fun onPostExecute(result: String) {
        if (result == "Error") {
            errorHandler?.let { it() }
        } else {
            callback?.let {
                it(result)
            }
        }
    }
}