package cn.ctcraft.ctonlinereward.utils;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class SerializableUtil {

    public byte[] singleObjectToByteArray(Object object) throws IOException {
        if (object instanceof ConfigurationSerializable || object instanceof Serializable) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(buf);
            out.writeObject(object);
            out.close();
            return buf.toByteArray();
        }
        return null;
    }


    public <T> T singleObjectFromString(String serialized, Class<T> classOfT) throws IOException {
        return singleObjectFromByteArray(Base64Coder.decodeLines(serialized), classOfT);
    }

    @SuppressWarnings("unchecked")
    public <T> T singleObjectFromByteArray(byte[] serialized, Class<T> classOfT) throws IOException {
        ByteArrayInputStream buf = new ByteArrayInputStream(serialized);
        BukkitObjectInputStream in = new BukkitObjectInputStream(buf);
        T object = null;

        try {
            object = (T) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        } finally {
            in.close();
        }

        return object;
    }


    public String singleObjectToString(Object object) throws IOException {
        byte[] raw = singleObjectToByteArray(object);

        if (raw != null) {
            return Base64Coder.encodeLines(raw);
        }

        return null;
    }
}
