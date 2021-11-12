package org.yingye.rdf.io;

import org.yingye.rdf.util.MD5Util;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

public class FileController {

    // 保存第一次找到的文件hash值和路径
    private final HashMap<String, String> specimen = new HashMap<>();
    // 是否删除文件
    private final Boolean delFile;
    // 是否顺带删除空文件夹
    private final Boolean delDir;
    // 要检查的目录
    private final File dir;
    private final BufferedWriter bw;
    // 状态回调, 在界面上显示现在在处理的文件名
    private final Consumer<String> cb;

    public FileController(File dir, String logPath, Boolean df, Boolean dr, Consumer<String> cb) throws FileNotFoundException {
        this.dir = dir;
        this.delFile = df;
        this.delDir = dr;
        this.cb = cb;

        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logPath + "out.log")));
    }

    // 决定方法调用
    public void exec() throws IOException {
        if (!this.delFile && !this.delDir) {
            check(this.dir);
        } else if (this.delFile) {
            remove(this.dir);
        } else {
            onlyRemoveEmptyDirectory(this.dir);
        }
        bw.close();
    }

    // 仅检查重复文件
    private void check(File target) throws IOException {
        File[] files = Objects.requireNonNull(target.listFiles());
        for (File file : files) {
            if (file.exists()) {
                cb.accept(file.getAbsolutePath());
                if (file.isDirectory()) {
                    check(file);
                } else {
                    String hash32 = MD5Util.md5HashCode32(file.getAbsolutePath());
                    if (specimen.containsKey(hash32)) {
                        bw.write("发现重复文件\n对比文件:" + specimen.get(hash32) + "\n本次重复文件:" + file.getAbsolutePath() + "\n\n");
                        bw.flush();
                    } else {
                        specimen.put(hash32, file.getAbsolutePath());
                    }
                }
            } else {
                bw.write(target.getAbsolutePath() + " 该文件可被列举出来, 但却被判断为不存在\n");
                bw.flush();
            }
        }
    }

    // 移除文件、文件夹
    private void remove(File target) throws IOException {
        File[] files = Objects.requireNonNull(target.listFiles());
        for (File file : files) {
            if (file.exists()) {
                cb.accept(file.getAbsolutePath());
                if (file.isDirectory()) {
                    remove(file);
                } else {
                    String hash32 = MD5Util.md5HashCode32(file.getAbsolutePath());
                    if (specimen.containsKey(hash32)) {
                        boolean delete = file.delete();
                        if (delete) {
                            bw.write("发现重复文件\n对比文件:" + specimen.get(hash32) + "\n本次重复文件:" + file.getAbsolutePath() + "\n文件成功删除\n\n");
                        } else {
                            bw.write("发现重复文件\n对比文件:" + specimen.get(hash32) + "\n本次重复文件:" + file.getAbsolutePath() + "\n尝试删除文件, 但删除失败\n\n");
                        }
                        bw.flush();
                    } else {
                        specimen.put(hash32, file.getAbsolutePath());
                    }
                }
            } else {
                bw.write(target.getAbsolutePath() + " 该文件可被列举出来, 但却被判断为不存在\n");
                bw.flush();
            }
        }
        if (Objects.requireNonNull(target.listFiles()).length == 0 && delDir) {
            boolean delete = target.delete();
            if (delete) {
                bw.write("文件夹删除成功 " + target.getAbsolutePath() + "\n\n");
            } else {
                bw.write("文件夹删除失败 " + target.getAbsolutePath() + "\n\n");
            }
        }
    }

    private void onlyRemoveEmptyDirectory(File target) throws IOException {
        File[] files = Objects.requireNonNull(target.listFiles());
        if (files.length == 0) {
            cb.accept(target.getAbsolutePath());
            boolean delete = target.delete();
            if (delete) {
                bw.write("文件夹删除成功 " + target.getAbsolutePath() + "\n");
            } else {
                bw.write("文件夹删除失败 " + target.getAbsolutePath() + "\n");
            }
        } else {
            for (File file : files) {
                if (file.isDirectory()) {
                    cb.accept(file.getAbsolutePath());
                    onlyRemoveEmptyDirectory(file);
                }
            }
            if (Objects.requireNonNull(target.listFiles()).length == 0) {
                boolean delete = target.delete();
                if (delete) {
                    bw.write("文件夹删除成功 " + target.getAbsolutePath() + "\n");
                } else {
                    bw.write("文件夹删除失败 " + target.getAbsolutePath() + "\n");
                }
            }
        }
    }

}
