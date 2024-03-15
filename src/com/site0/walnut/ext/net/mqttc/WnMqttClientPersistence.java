package com.site0.walnut.ext.net.mqttc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;

public class WnMqttClientPersistence implements MqttClientPersistence {

    protected WnIo io;
    protected WnObj parent;
    protected String serverURI;
    protected String serverPath;

    public WnMqttClientPersistence(WnIo io, WnObj parent) {
        this.io = io;
        this.parent = parent;
    }

    @Override
    public void open(String clientId, String serverURI) throws MqttPersistenceException {
        this.serverURI = serverURI;
        this.serverPath = "persistence/" + Wlang.md5(serverURI) + "/";
    }

    @Override
    public void close() throws MqttPersistenceException {
        this.serverURI = null;
    }

    @Override
    public void put(String key, MqttPersistable persistable) throws MqttPersistenceException {
        try {
            WnObj wobj = io.createIfExists(parent, serverPath + key, WnRace.FILE);
            ByteArrayOutputStream bao = new ByteArrayOutputStream(persistable.getHeaderLength() + persistable.getPayloadLength() + 4 + 4);
            DataOutputStream dos = new DataOutputStream(bao);
            dos.writeInt(persistable.getHeaderLength());
            dos.writeInt(persistable.getPayloadLength());
            dos.write(persistable.getHeaderBytes());
            dos.write(persistable.getPayloadBytes());
            dos.close();
            io.writeAndClose(wobj, new ByteArrayInputStream(bao.toByteArray()));
        }
        catch (Exception e) {
            throw new MqttPersistenceException(e);
        }
    }

    @Override
    public MqttPersistable get(String key) throws MqttPersistenceException {
        try {
            WnObj wobj = io.fetch(parent, serverPath + key);
            if (wobj == null)
                return null;
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            io.readAndClose(wobj, bao);
            byte[] buf = bao.toByteArray();
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf));
            int headerLength = dis.readInt();
            int payloadLength = dis.readInt();
            return new MqttPersistable() {

                public byte[] getHeaderBytes() throws MqttPersistenceException {
                    return buf;
                }

                public int getHeaderLength() throws MqttPersistenceException {
                    return headerLength;
                }

                public int getHeaderOffset() throws MqttPersistenceException {
                    return 8;
                }

                public byte[] getPayloadBytes() throws MqttPersistenceException {
                    return buf;
                }

                public int getPayloadLength() throws MqttPersistenceException {
                    return payloadLength;
                }

                public int getPayloadOffset() throws MqttPersistenceException {
                    return 8 + headerLength;
                }

            };
        }
        catch (Exception e) {
            throw new MqttPersistenceException(e);
        }
    }

    @Override
    public void remove(String key) throws MqttPersistenceException {
        WnObj wobj = io.fetch(parent, serverPath + key);
        if (wobj != null)
            io.delete(wobj);
    }

    @Override
    public Enumeration<String> keys() throws MqttPersistenceException {
        WnObj wobj = io.fetch(parent, serverPath);
        if (wobj == null)
            return Collections.emptyEnumeration();
        List<WnObj> objs = io.getChildren(wobj, null);
        List<String> names = new ArrayList<>(objs.size());
        for (WnObj wobj2 : objs) {
            names.add(wobj2.name());
        }
        return Collections.enumeration(names);
    }

    @Override
    public void clear() throws MqttPersistenceException {
        WnObj wobj = io.fetch(parent, serverPath);
        if (wobj != null)
            io.delete(wobj, true);
    }

    @Override
    public boolean containsKey(String key) throws MqttPersistenceException {
        return io.exists(parent, serverPath + key);
    }
}
