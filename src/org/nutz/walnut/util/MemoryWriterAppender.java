package org.nutz.walnut.util;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.nutz.repo.cache.simple.LRUCache;

public class MemoryWriterAppender extends WriterAppender {

    public static LRUCache<String, String> cache = new LRUCache<>(1024);
    protected static AtomicLong atom = new AtomicLong();

    public MemoryWriterAppender() {
        setWriter(new Writer() {
            public void write(char[] cbuf, int off, int len) throws IOException {}

            public void flush() throws IOException {}

            public void close() throws IOException {}
        });
    }

    protected void subAppend(LoggingEvent event) {
        String line = this.layout.format(event);
        cache.put(atom.getAndIncrement()+"", line);
        String[] s = event.getThrowableStrRep();
        if (s != null) {
            int len = s.length;
            for (int i = 0; i < len && i < 10; i++) {
                cache.put(atom.getAndIncrement()+"", s[i]);
            }
        }
    }
}
