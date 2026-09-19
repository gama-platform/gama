import re

with open('./gama.dependencies/build.properties', 'r') as f:
    content = f.read()

eclipse_serializer_jars = """               libs/eclipse-serializer/afs-2.1.3.jar,\\
               libs/eclipse-serializer/base-2.1.3.jar,\\
               libs/eclipse-serializer/persistence-2.1.3.jar,\\
               libs/eclipse-serializer/persistence-binary-2.1.3.jar,\\
               libs/eclipse-serializer/serializer-2.1.3.jar,\\
               libs/eclipse-serializer/slf4j-api-2.0.12.jar,\\
"""

content = content.replace("               libs/Java-WebSocket 1.6.0/Java-WebSocket-1.6.0.jar,\\\n", "               libs/Java-WebSocket 1.6.0/Java-WebSocket-1.6.0.jar,\\\n" + eclipse_serializer_jars)

with open('./gama.dependencies/build.properties', 'w') as f:
    f.write(content)
