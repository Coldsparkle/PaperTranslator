package ml.areostech;

import ml.areostech.translators.*;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.content.model.DocumentSection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/*
 * Created by Coldsparkle on 2019-05-09 15:15
 * Email:Coldsparkle@outlook.com
 */
public class MainWindow {

    private JTextField textFiled1;
    private JButton button1;
    private JPanel jpanel;
    private JButton button2;
    private JTextArea textarea1;
    private JLabel label;
    private JComboBox comboBox1;
    private File pdfFile = null;
    private JFileChooser chooser = new JFileChooser(".");

    private int srcIndex = 0;
    private ArrayList<Translator> translators = new ArrayList<Translator>();

    private static final String YOUDAO_WEB = "有道web";
    private static final String YOUDAO_OPEN = "有道OPEN";
    private static final String BAIDU = "*百度翻译";
    private static final String COPYRIGHT = "Powered by CERMINE. Developed by Coldsparkle. ";
    private static final String VERSION_NAME = "翻译猫 v1.1-hotfix";
    private static final String CHOOSE_PDF_HINT = "输入PDF路径或点击右侧按钮选取PDF文件";
    private static final String CHOOSE_PDF_BTN_TEXT = "选取PDF";
    private static final String TRANSLATE_BTN_TEXT = "翻译";
    private static final String FILE_NOT_EXISTS = "文件不存在。";

    private MainWindow() {
        initComponent();
        initEvent();
    }

    private void initComponent() {
        label.setText(COPYRIGHT + VERSION_NAME);
        textFiled1.setText(CHOOSE_PDF_HINT);
        button1.setText(CHOOSE_PDF_BTN_TEXT);
        button2.setText(TRANSLATE_BTN_TEXT);
        textarea1.setText(COPYRIGHT);
        comboBox1.addItem(YOUDAO_WEB);
        comboBox1.addItem(YOUDAO_OPEN);
        comboBox1.addItem(BAIDU);
        textarea1.setEditable(false);
    }

    private void initEvent() {
        chooser.setFileFilter(new PdfFilter());
        translators.add(new YoudaoWebTranslator());
        translators.add(new YoudaoOpenTranslator());
        translators.add(new BaiduTranslator());
        chooser.setFileSelectionMode(0);
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int status = chooser.showOpenDialog(null);
                if (status != 1) {
                    pdfFile = chooser.getSelectedFile();
                    textFiled1.setText(pdfFile.getAbsolutePath());
                }
            }
        });
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (pdfFile == null) {
                    pdfFile = new File(textFiled1.getText());
                    if (!pdfFile.exists()) {
                        textarea1.setText(textarea1.getText() + "\n" + FILE_NOT_EXISTS);
                        pdfFile = null;
                        return;
                    }
                }
                textarea1.setText(COPYRIGHT);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        beginTranslate(pdfFile);
                    }
                }.start();
            }
        });
        comboBox1.setSelectedIndex(0);
        comboBox1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    String s = e.getItem().toString();
                    if (s.equals(YOUDAO_WEB)) {
                        srcIndex = 0;
                    } else if (s.equals(YOUDAO_OPEN)){
                        srcIndex = 1;
                    } else {
                        srcIndex = 2;
                    }
                }
            }
        });
    }

    private void beginTranslate(File pdfFile) {
        FileInputStream pdfInPutFileInputStream = null;
        BufferedWriter writer = null;
        try {
            long begin = new Date().getTime();
            println("开始解析...");
            enableBtn(false);
            pdfInPutFileInputStream = new FileInputStream(pdfFile);
            ContentExtractor contentExtractor = new ContentExtractor();
            contentExtractor.setPDF(pdfInPutFileInputStream);
            FileInfo fileInfo = new FileInfo(pdfFile);
            File outFile = new File(fileInfo.getPath(), fileInfo.getName() + ".txt");
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));
            int sectionIndex = 1;
            boolean hasParsed = false;
            for (DocumentSection section : contentExtractor.getBody().getSections()) {
                if(!hasParsed) {
                    hasParsed = true;
                    println("解析完成，开始翻译文档：EN -> ZHS");
                }

                translateSection(section, writer, sectionIndex++, translators.get(srcIndex));
                writer.flush();
            }
            pdfInPutFileInputStream.close();
            writer.close();
            println("翻译完成！耗时：" + (new Date().getTime() - begin) / 1000f + "s");
            println("结果保存于" + outFile.getAbsolutePath());
            enableBtn(true);
        } catch (Exception e) {
            println("<=======错误日志开始=======>");
            e.printStackTrace();
            println(e.getMessage());
            println("<=======错误日志结束=======>");
            enableBtn(true);
        } finally {
            try {
                if (pdfInPutFileInputStream != null) {
                    pdfInPutFileInputStream.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void println(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String textarea1Text = textarea1.getText();
                textarea1.setText(textarea1Text + "\n" + text);
            }
        });
    }

    private void enableBtn(final boolean enable) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                button1.setEnabled(enable);
                button2.setEnabled(enable);
                comboBox1.setEnabled(enable);
            }
        });
    }

    private void buildSectionList(ArrayList<DocumentSection> list, DocumentSection section) {
        list.add(section);
        for (DocumentSection subsection : section.getSubsections()) {
            buildSectionList(list, subsection);
        }
    }

    private void translateSection(DocumentSection documentSection,
                                  BufferedWriter writer,
                                  int sectionIndex,
                                  Translator translator) throws Exception {
        ArrayList<DocumentSection> sectionList = new ArrayList<DocumentSection>();
        buildSectionList(sectionList, documentSection);
        int paraIndex = 1;

        for (DocumentSection section : sectionList) {
            writer.write(section.getTitle().replace("\n", ""));
            writer.newLine();

            for (String paragraph : section.getParagraphs()) {
                println("开始翻译第" + sectionIndex + "节第" + paraIndex++ + "段");
                String text = paragraph.replace("\n", " ");
                TranslateResult translateResult = translator.translate(text);
                Thread.sleep(500);
                if (translateResult.getErrorCode() != 0) {
                    println("翻译错误: " + translateResult.getErrorCode());
                    println(text);
                    writer.write("    " + text);
                    writer.newLine();
                    continue;
                }
                writer.write("    " + translateResult.getResult());
                writer.newLine();
            }
            writer.newLine();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("翻译🐱");
        MainWindow mainWindow = new MainWindow();
        frame.setContentPane(mainWindow.jpanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
