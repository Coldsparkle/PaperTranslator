package ml.areostech

import java.io.File
import javax.swing.filechooser.FileFilter


/*
* Created by Coldsparkle on 2019/5/12 00:25
* Email:Coldsparkle@outlook.com
*/
class PdfFilter : FileFilter() {
    override fun accept(f: File?): Boolean {
        if (f == null) {
            return false
        } else {
            return f.isDirectory || f.name.toLowerCase().endsWith(".pdf")
        }
    }

    override fun getDescription() = "*.pdf"
}