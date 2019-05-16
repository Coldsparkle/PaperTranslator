package ml.areostech.translators

import com.alibaba.fastjson.JSON
import java.lang.StringBuilder
import java.net.URL
import java.net.URLEncoder

/*
* Created by Coldsparkle on 2019-05-09 13:35
* Email:Coldsparkle@outlook.com
*/
class YoudaoOpenTranslator : Translator {
    companion object {
        const val YOUDAO_BASE_URL = "http://fanyi.youdao.com/translate?&doctype=json&type=AUTO&i="
    }

    override fun translate(text: String): TranslateResult {
        val url = URL("$YOUDAO_BASE_URL${URLEncoder.encode(text, "utf-8")}")
        val jsonText = url.readText().trim()
        val youdaoResponse = JSON.parseObject(jsonText, YoudaoResponse::class.java)
        return youdaoResponse.parse()
    }
}

class YoudaoResult {
    var src: String? = null
    var tgt: String? = null
}

class YoudaoResponse : TranslateParser {
    override fun parse(): TranslateResult {
        val result = TranslateResult()
        result.errorCode = errorCode
        if (errorCode == 0) {
            translateResult?.let {
                val builder = StringBuilder()
                for (list in it) {
                    for (youdaoResult in list) {
                        builder.append(youdaoResult.tgt)
                    }
                }
                result.result = builder.toString()
            }
        }
        return result
    }

    var type: String? = null
    var errorCode: Int? = 0
    var elapsedTime: Int? = 0
    var translateResult: List<List<YoudaoResult>>? = null
}

fun main() {
    val result = YoudaoOpenTranslator().translate("I used to question who I was, well now I see,\n" +
            "The answers' in your eyes")
    println(result.result)
}