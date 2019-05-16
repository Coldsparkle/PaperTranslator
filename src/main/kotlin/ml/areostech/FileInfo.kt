package ml.areostech

import org.bouncycastle.asn1.x500.style.RFC4519Style.name
import java.io.File


/*
* Created by Coldsparkle on 2019-05-09 16:02
* Email:Coldsparkle@outlook.com
*/
class FileInfo(file: File) {
    val path: String
    val name: String
    val type: String
    init {
        val absolutePath = file.absolutePath
        val lastIndex = absolutePath.lastIndexOf(File.separator)
        path = absolutePath.substring(0, lastIndex)
        val split = absolutePath.substring(lastIndex + 1, absolutePath.length).split(""".""")
        name = split[0]
        type = split[1]
    }
}