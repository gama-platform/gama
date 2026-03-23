package gama.extension.serialize.fst.serializers;

import java.io.IOException;
import java.math.BigInteger;

import gama.extension.serialize.fst.FSTBasicObjectSerializer;
import gama.extension.serialize.fst.FSTClazzInfo;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

/**
 * author: nicoruti
 * submitted via #53
 *
 */
public class FSTBigIntegerSerializer  extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy,
                            int streamPosition) throws IOException {
        byte[] value = ((BigInteger) toWrite).toByteArray();
        out.writeInt(value.length);
        out.write(value);
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee,
                              int streamPosition) throws Exception {
        int len = in.readInt();
        byte[] buf = new byte[len];
        in.read(buf);
        BigInteger bigInteger = new BigInteger(buf);
        in.registerObject(bigInteger,streamPosition,serializationInfo,referencee);
        return bigInteger;
    }
}
