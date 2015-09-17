package org.nutz.walnut.impl.box;

/**
 * 对 Unix/Linux 终端字符颜色的一些操作
 * <p>
 * 要改变输出文字的颜色其实很简单，我们先来个hello world：
 * 
 * <pre>
 * echo -e "\033[0;31;40mhello world"
 * </pre>
 * <p>
 * 根据上面的代码，我来解释一下它的语法结构。
 * </p>
 * <p>
 * 首先，”“里面的hello world应该很好理解了，就是我们要输出的内容，关键是前面的 <b>\033[0;31;40m</b>，<br>
 * 这里需要遵循一定的规律。
 * </p>
 * <p>
 * 其中<b>\033</b>指的是33（8进制）代表escape字符，与”<b>[</b>“ 连起来就是设置字符颜色的标志 。<br>
 * 也就是说，当xterm看到”\033[“时就知道后面接的是要打印的字符的颜色设定。<br>
 * 所以每次设定颜色的时候都要以这个开头。当然， 你也可以用16进制来表示这个开头：
 * </p>
 * 
 * <pre class="code bash">
 * echo -e "\x1b\x5b0;31;40mhello world"
 * </pre>
 * <p>
 * 或者用”\e[“：
 * </p>
 * 
 * <pre class="code bash">
 * echo -e "\e[0;31;40mhello world"
 * </pre>
 * <p>
 * “\033[“后面接的是三个参数，都用数字给出并以”;”号分隔。他们分别代表字体属性，字体颜色与背景颜色。
 * </p>
 * <p>
 * 以我们的hello world为例，0代表重置原来的所有属性，31是红色的代码，40则是黑色背景的代码。
 * </p>
 * <p>
 * 关于具体每个参数有哪些可选的值，请看下面。
 * </p>
 * <p>
 * <b>字体属性:</b>
 * 
 * <pre>
 * 0 Reset All Attributes (return to normal mode)
 * 1 Bright (usually turns on BOLD)
 * 2 Dim
 * 3 Underline
 * 5 Blink
 * 7 Reverse
 * 8 Hidden
 * </pre>
 * 
 * <p>
 * <b>字体颜色:</b>
 * 
 * <pre>
 * 30 black
 * 31 dark red
 * 32 light green
 * 33 dark yellow
 * 34 dark blue
 * 35 light violet
 * 36 light blue, cyan
 * 37 white
 * </pre>
 * 
 * <p>
 * <b>背景颜色:</b>
 * 
 * <pre>
 * 40 Black
 * 41 Red
 * 42 Green
 * 43 Yellow
 * 44 Blue
 * 45 Magenta
 * 46 Cyan
 * 47 White
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * 比如，我在 linux 上执行 <code>"ls --color"</code>，得到的输出
 * 
 * <pre>
 *  ^  [  0  m  ^  [  0  1  ;  3  4  m  b  i  n  ^  [  0  m 
 *  1b 5b 30 6d 1b 5b 30 31 3b 33 34 6d 62 69 6e 1b 5b 30 6d
 * 
 *  ^  [  0  1  ;  3  4  m  D  o  w  n  l  o  a  d  ^  [  0  m 
 *  1b 5b 30 31 3b 33 34 6d 44 6f 77 6e 6c 6f 61 64 1b 5b 30 6d
 * 
 *  ^  [  0  1  ;  3  4  m  g  o  p  r  o  j  ^  [  0  m 
 *  1b 5b 30 31 3b 33 34 6d 67 6f 70 72 6f 6a 1b 5b 30 6d
 * 
 *  ^  [  0  1  ;  3  4  m  w  o  r  k  s  p  a  c  e  ^  [  0  m 
 *  1b 5b 30 31 3b 33 34 6d 77 6f 72 6b 73 70 61 63 65 1b 5b 30 6d
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @see <a href=
 *      "http://www.cnblogs.com/icejoywoo/archive/2011/05/28/2061323.html">
 *      linux终端输出带有颜色的字体</a>
 */
public abstract class LinuxTerminal {

    private final static String _HT = new String(new byte[]{0x1b, 0x5b, 0x30, 0x6d});

    private final static String _L = new String(new byte[]{0x1b, 0x5b});

    private final static String _R = "m";

    public static String wrapFont(String str, int font, int color, int bgcolor) {
        StringBuilder sb = new StringBuilder(_HT).append(_L);
        sb.append(String.format("%02d", font)).append(';');
        sb.append(String.format("%02d", color));
        if (bgcolor >= 40 && bgcolor <= 47) {
            sb.append(';').append(String.format("%02d", bgcolor));
        }
        return sb.append(_R).append(str).append(_HT).toString();
    }

    public static String wrapFont(String str, int font, int color) {
        return wrapFont(str, font, color, -1);
    }

    public static String unwrapFont(String str) {
        StringBuilder sb = new StringBuilder();
        int l = 0;
        int r = 0;
        char[] cs = str.toCharArray();
        for (; r < cs.length; r++) {
            char b = cs[r];
            // 遇到颜色
            if (b == 0x1b) {
                // 有字符串，先消费
                if (r > l) {
                    sb.append(new String(cs, l, r - l));
                }
                // 试图读取控制码
                r += 2;
                l = r;
                for (; r < cs.length; r++) {
                    char c = cs[r];
                    if (c == 'm')
                        break;
                }
                // this._font.parse(s.substring(l,r));
                l = r + 1;
            }
        }
        // 最后输出剩余的
        if (r > l) {
            sb.append(new String(cs, l, r));
        }
        // 返回
        return sb.toString();
    }

}
