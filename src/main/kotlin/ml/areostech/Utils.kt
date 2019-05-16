package ml.areostech

import java.security.MessageDigest

/*
* Created by Coldsparkle on 2019-05-11 19:25
* Email:Coldsparkle@outlook.com
*/
fun md5(text: String) : String{
    val digest = MessageDigest.getInstance("md5").digest(text.toByteArray())
    val j = digest.size
    val hexDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f')
    val str = CharArray(2 * j)
    var k = 0

    for (byte0 in digest) {
        str[k++] = hexDigits[byte0.toInt() shr 4 and  0xf];
        str[k++] = hexDigits[byte0.toInt() and  0xf];
    }
    return String(str);
}