import re

with open('./gama.dependencies/META-INF/MANIFEST.MF', 'r') as f:
    content = f.read()

# Add exports
export_packages = """ org.eclipse.serializer,
 org.eclipse.serializer.afs,
 org.eclipse.serializer.collections,
 org.eclipse.serializer.collections.lazy,
 org.eclipse.serializer.concurrency,
 org.eclipse.serializer.configuration,
 org.eclipse.serializer.memory,
 org.eclipse.serializer.meta,
 org.eclipse.serializer.persistence,
 org.eclipse.serializer.persistence.binary,
 org.eclipse.serializer.persistence.types,
 org.eclipse.serializer.reference,
 org.eclipse.serializer.reflect,
 org.eclipse.serializer.typing,
 org.eclipse.serializer.util,
"""

content = content.replace("Export-Package: au.com.objectix.jgridshift,\n", "Export-Package: au.com.objectix.jgridshift,\n" + export_packages)

# Add Bundle-Classpath
classpath_jars = """ libs/eclipse-serializer/afs-2.1.3.jar,
 libs/eclipse-serializer/base-2.1.3.jar,
 libs/eclipse-serializer/persistence-2.1.3.jar,
 libs/eclipse-serializer/persistence-binary-2.1.3.jar,
 libs/eclipse-serializer/serializer-2.1.3.jar,
 libs/eclipse-serializer/slf4j-api-2.0.12.jar,
"""

content = content.replace("Bundle-ClassPath: .,\n", "Bundle-ClassPath: .,\n" + classpath_jars)

with open('./gama.dependencies/META-INF/MANIFEST.MF', 'w') as f:
    f.write(content)
