package org.yingye.rdf.ui;

import org.yingye.rdf.io.FileController;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;

public class Interface extends JFrame implements ActionListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private final JPanel body = new JPanel();
    private final JTextField path = new JTextField("", 20);
    private final JButton button = new JButton("选择目录");

    private final JPanel items = new JPanel();
    private final JCheckBox del = new JCheckBox("删除重复文件");
    private final JCheckBox empty = new JCheckBox("删除空目录");

    private final JPanel bottom = new JPanel();
    private final JButton duplication = new JButton("执行");
    private final JButton exit = new JButton("退出");
    private final JLabel label = new JLabel(" ");

    private final JFileChooser chooser = new JFileChooser();

    private boolean isRunning = false;

    public Interface(int width, int height) {
        this.setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/favicon.png"))).getImage());
        this.setSize(width, height);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setTitle("文件去重");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        set();
        listener();
        this.setVisible(true);
    }

    private void set() {
        // 功能控件
        body.add(path);
        body.add(button);
        this.add(body, BorderLayout.NORTH);

        // 选项
        items.add(del);
        items.add(empty);
        this.add(items, BorderLayout.CENTER);

        // 底部控件
        bottom.setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.add(duplication);
        panel.add(exit);
        bottom.add(panel, BorderLayout.SOUTH);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        bottom.add(label, BorderLayout.NORTH);
        this.add(bottom, BorderLayout.SOUTH);

        // 选择框
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    private void listener() {
        button.addActionListener(this);
        exit.addActionListener(this);
        duplication.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(button)) {
            this.processingDirectory();
        } else if (e.getSource().equals(exit)) {
            if (isRunning) {
                if (JOptionPane.showConfirmDialog(this, "操作执行中, 是否继续关闭?", "警告", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) == 0) {
                    dispose();
                }
            } else {
                dispose();
            }
        } else if (e.getSource().equals(duplication)) {
            this.startExecution();
        }
    }


    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            if (isRunning) {
                if (JOptionPane.showConfirmDialog(this, "操作执行中, 是否继续关闭?", "警告", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE) == 0) {
                    dispose();
                } else {
                    return;
                }
            } else {
                super.processWindowEvent(e); //该语句会执行窗口事件的默认动作(如：隐藏)
            }
        }
        super.processWindowEvent(e); //该语句会执行窗口事件的默认动作(如：隐藏)
    }

    private void processingDirectory() {
        int i = chooser.showOpenDialog(null);
        if (i != 1) {
            File file = chooser.getSelectedFile();
            // 将选中的文件路径放置在界面上
            path.setText(file.getAbsolutePath());
        }
    }

    private void startExecution() {
        // 获取要检查的文件夹路径
        String dirPath = path.getText().trim();

        // 如果路径为空，则不进行任何操作
        if (dirPath.equals("")) {
            JOptionPane.showMessageDialog(this, "未指定路径");
            return;
        }

        // 判断路径是不是一个文件夹
        File file = new File(dirPath);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "该路径不存在");
            return;
        }
        if (!file.isDirectory()) {
            JOptionPane.showMessageDialog(this, "指定的路径不是一个文件夹");
            return;
        }

        // 获取勾选状态
        Boolean enableDel = del.isSelected();
        Boolean delEmpty = empty.isSelected();

        // 询问是否进行删除操作
        if (enableDel || delEmpty) {
            if (JOptionPane.showConfirmDialog(this, "确认进行删除操作吗?", "警告", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) != 0) {
                JOptionPane.showMessageDialog(this, "本次操作取消");
                return;
            }
        }

        // 操作日志输出的文件夹
        String outPath = "";

        this.isRunning = true;
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                FileController fileController = new FileController(file, outPath, enableDel, delEmpty, label::setText);
                fileController.exec();
                return 0;
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage());
                return 1;
            }
        });

        // 异步任务，防止程序无响应
        future.thenAccept(unused -> {
            isRunning = false;
            label.setText(" ");
            if (unused == 0) {
                JOptionPane.showMessageDialog(this, "操作已完成\n日志路径为: " + new File(outPath + "out.log").getAbsolutePath());
            }

            // 执行完后调用一次GC
            System.gc();
        });

    }

}