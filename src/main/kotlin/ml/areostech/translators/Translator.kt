package ml.areostech.translators


/*
* Created by Coldsparkle on 2019-05-09 15:41
* Email:Coldsparkle@outlook.com
*/
interface Translator {
    fun translate(text: String) : TranslateResult
}

class TranslateResult {
    var errorCode: Int? = 0
    var result: String? = ""
}

interface TranslateParser {
    fun parse() : TranslateResult
}