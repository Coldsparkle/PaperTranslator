package ml.areostech.translators

import com.alibaba.fastjson.JSON
import ml.areostech.md5
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.lang.RuntimeException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*


/*
* Created by Coldsparkle on 2019-05-10 09:56
* Email:Coldsparkle@outlook.com
*/
class YoudaoWebTranslator : Translator {
    companion object {
        const val BASE_URL = "http://fanyi.youdao.com"
        const val TRANSLATE_URL = "$BASE_URL/translate_o?smartresult=dict&smartresult=rule"
    }
    var baseYoudaoCookies: MutableList<String>
    var requestCount = 1
    init {
        baseYoudaoCookies = getBaseCookies()
    }

    override fun translate(text: String): TranslateResult {
        if (requestCount % 30 == 0) {
            baseYoudaoCookies = getBaseCookies()
            println(baseYoudaoCookies)
        }
        val conn = URL(TRANSLATE_URL).openConnection() as HttpURLConnection
        val ts = Date().time.toString()
        val salt = "$ts${(Math.random() * 10).toInt()}"
        val sign = md5("fanyideskweb$text$salt@6f#X3=cCuncYssPsuRUE")
        val bv = "1e9538f95b23257ede9acdc941c8e1f8"
        val i =  URLEncoder.encode(text, "utf-8")
        conn.apply {
            doOutput = true
            requestMethod = "POST"
            setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01")
            //setRequestProperty("Accept-Encoding", "text")
            setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9")
            setRequestProperty("Connection", "keep-alive")
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            setRequestProperty("DNT", "1")
            setRequestProperty("X-Requested-With", "XMLHttpRequest")
            setRequestProperty("Referer", BASE_URL)
            setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
            setRequestProperty("Host", BASE_URL)
            setRequestProperty("Origin", BASE_URL)
            val cookieString = buildCookieString()
            setRequestProperty("Cookie", cookieString)
            OutputStreamWriter(conn.outputStream).apply {
                write("i=$i&from=AUTO&to=AUTO&smartresult=dict&client=fanyideskweb&salt=$salt&sign=$sign&ts=$ts&bv=$bv&doctype=json&version=2.1&keyfrom=fanyi.web&action=FY_BY_REALTlME")
                flush()
            }
            requestCount++
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader(inputStream, "utf-8").apply {
                    for (s in readLines()) {
                        try {
                            val youdaoResponse = JSON.parseObject(s, YoudaoResponse::class.java)
                            return youdaoResponse.parse()
                        } catch (e: Exception) {
                            throw RuntimeException("${s}\n本机IP请求次数过多，已被有道封禁。\n请1小时后再试或切换备用百度源")
                        }
                    }
                }
            }
        }

        return TranslateResult()
    }

    private fun getBaseCookies(): MutableList<String> {
        val urlConnection = URL(BASE_URL).openConnection() as HttpURLConnection
        urlConnection.requestMethod = "GET"
        urlConnection.connect()
        if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
            val cookies = mutableListOf<String>()

            urlConnection.headerFields["Set-Cookie"]?.apply {
                for (str in this) {
                    val split = str.split(";")
                    val findJsession = split.find { s -> s.contains("JSESSIONID") }
                    val findUserId = split.find { s -> s.contains("OUTFOX_SEARCH_USER_ID") }
                    if (findJsession != null) {
                        cookies.add(findJsession)
                    }
                    if (findUserId != null) {
                        cookies.add(findUserId)
                    }
                }
            }
            return cookies
        }
        return mutableListOf<String>()
    }

    private fun buildCookieString() = buildString {
        for (c in baseYoudaoCookies) {
            append(c)
            append(";")
        }
        append("YOUDAO_MOBILE_ACCESS_TYPE=1;")

        append("OUTFOX_SEARCH_USER_ID_NCOO=${(2147483647 * Math.random()).toBigDecimal()};")
        append("___rl__test__cookies=${Date().time}")
    }
}

fun main() {
    val translate =
        YoudaoWebTranslator().translate("Adversarial attacks on machine learning models have seen increasing interest in the past years. By making only subtle changes to the input of a convolutional neural network, the output of the network can be swayed to output a completely different result. The first attacks did this by changing pixel values of an input image slightly to fool a classifier to output the wrong class. Other approaches have tried to learn “patches” that can be applied to an object to fool detectors and classifiers. Some of these approaches have also shown that these attacks are feasible in the realworld, i.e. by modifying an object and filming it with a video camera. However, all of these approaches target classes that contain almost no intra-class variety (e.g. stop signs). The known structure of the object is then used to generate an adversarial patch on top of it.")
    println(translate.result)
}