package gama.extension.serialize.fst.serializers;

import java.io.IOException;
import java.lang.reflect.Constructor;

import gama.extension.serialize.fst.FSTBasicObjectSerializer;
import gama.extension.serialize.fst.FSTClazzInfo;
import gama.extension.serialize.fst.FSTObjectInput;
import gama.extension.serialize.fst.FSTObjectOutput;

public class FSTThrowableSerializer extends FSTBasicObjectSerializer {
    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo,
                            FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        Throwable t = (Throwable)toWrite;
        out.writeStringUTF(t.getMessage());
        StackTraceElement[] ste = t.getStackTrace();
        out.writeObject(ste);
        out.writeObject(t.getCause());
        out.writeObject(t.getSuppressed());
    }


    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo,
                              FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        Constructor<? extends  Throwable> constructor = objectClass.getConstructor(String.class);
        Throwable t = constructor.newInstance(in.readStringUTF()); // This causes stack trace to be filled in twice but not an easy way to solve
        StackTraceElement[] ste = (StackTraceElement[]) in.readObject();
        if (ste!=null)
            t.setStackTrace(ste);
        t.initCause((Throwable) in.readObject());
        Throwable[] suppressed = (Throwable[]) in.readObject();
        for (Throwable s : suppressed)
            t.addSuppressed(s);
        return t;
    }
}
