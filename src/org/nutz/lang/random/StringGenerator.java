package org.nutz.lang.random;

/**
 * @author zozohtnt
 * @author wendal(wendal1985@gmail.com)
 */
public class StringGenerator {

    /**
     * 
     * @param max
     *            必须大于0
     */
    public StringGenerator(int max) {
        this(1, max);
    }

    /**
     * 
     * @param min
     *            必须大于0
     * @param max
     *            必须不小于min
     */
    public StringGenerator(int min, int max) {
        this(min, max, CharGenerator.me());
    }

    public StringGenerator(int min, int max, CharGenerator cg) {
        this.maxLen = max;
        this.minLen = min;
        this.cg = cg;
    }

    private CharGenerator cg;

    /**
     * min length of the string
     */
    private int maxLen;

    /**
     * max length of the string
     */
    private int minLen;

    /**
     * 
     * @param min
     *            必须大于0
     * @param max
     *            必须不小于min
     */
    public void setup(int min, int max) {
        minLen = min;
        maxLen = max;
    }

    public CharGenerator getCharGenerator() {
        return cg;
    }

    public StringGenerator setCharGenerator(CharGenerator cg) {
        this.cg = cg;
        return this;
    }

    /**
     * 根据设置的max和min的长度,生成随机字符串.
     * <p/>
     * 若max或min小于0,则返回null
     * 
     * @return 生成的字符串
     */
    public String next() {
        if (maxLen <= 0 || minLen <= 0 || minLen > maxLen)
            return null;
        char[] buf = new char[R.random(minLen, maxLen)];
        for (int i = 0; i < buf.length; i++)
            buf[i] = CharGenerator.next36();
        return new String(buf);
    }

}
