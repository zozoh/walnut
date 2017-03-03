package org.nutz.walnut.ext.www;

public interface WnmlRuntime {

    /**
     * @param path
     *            绝对路径
     * @return 该路径下指向的文件内容
     */
    String readPath(String path);

    /**
     * @param cmdText
     *            命令内容
     * @return 命令的输出结果
     */
    String exeCommand(String cmdText);

}
