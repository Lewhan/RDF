package org.yingye.rdf;

import org.yingye.rdf.ui.Interface;

import javax.swing.*;

public class Application {

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        if (JOptionPane.showConfirmDialog(null, "由于是根据文件内容进行判断，会对部分文件造成误判，是否继续使用?", "使用询问", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE) == 0) {
            new Interface(420, 160);
        }
    }

}
