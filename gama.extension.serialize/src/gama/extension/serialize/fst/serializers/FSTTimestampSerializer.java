package gama.extension.serialize.fst.serializers;

import java.io.IOException;

import gama.extension.serialize.fst.FSTBasicObjectSerializer;
import gama.extension.serialize.fst.FSTClazzInfo;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;
import gama.extension.serialize.fst.FSTClazzInfo.FSTFieldInfo;

public class FSTTimestampSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo,
                            FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        out.writeLong(((java.sql.Timestamp)toWrite).getTime());
    }

    @Override
    public boolean alwaysCopy(){
        return true;
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo,
                              FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        long l = in.readLong();
        Object res = new java.sql.Timestamp(l);
        return res;
    }
}
