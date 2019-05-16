package ml.areostech.translators

import com.alibaba.fastjson.JSON
import ml.areostech.md5
import java.lang.StringBuilder
import java.net.URL
import java.net.URLEncoder
import java.util.*

/*
* Created by Coldsparkle on 2019-05-09 21:58
* Email:Coldsparkle@outlook.com
*/
class BaiduTranslator : Translator {
    companion object {
        const val BASE_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate?from=en&to=zh&q="
        const val APPID = "" //替换为你的appid
        const val APP_KEY = "" //替换为你的app key
    }


    override fun translate(text: String) : TranslateResult {
        val salt = Date().time
        val sign = sign(text, salt)
        val url = "$BASE_URL${URLEncoder.encode(text, "utf-8")}&appid=$APPID&salt=$salt&sign=$sign"
        val jsonText = URL(url).readText()
        val baiduResponse = JSON.parseObject(jsonText, BaiduResponse::class.java)
        return baiduResponse.parse()
    }


    private fun sign(text: String, salt: Long) : String{
        val signText = APPID + text + salt + APP_KEY
        return md5(signText)
    }
}

class BaiduResult {
    var src: String? = null
    var dst: String? = null
}

class BaiduResponse : TranslateParser {
    override fun parse(): TranslateResult {
        val result = TranslateResult()
        result.errorCode = error_code
        if (error_code == 0) {
            trans_result?.let {
                val builder = StringBuilder()
                for (baiduResult in it) {
                    builder.append(baiduResult.dst)
                }
                result.result = builder.toString()
            }
        }
        return result
    }
    var error_code: Int? = 0
    var error_message: String? = null
    var from: String? = null
    var to: String? = null
    var trans_result: List<BaiduResult>? = null
}

fun main() {
    val translate = BaiduTranslator().translate("I used to question who I was, well now I see,\nThe answers' in your eyes")
    if (translate.errorCode == 0) {
        println(translate.result)
    }
}