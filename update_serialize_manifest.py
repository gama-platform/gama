import re

with open('./gama.extension.serialize/META-INF/MANIFEST.MF', 'r') as f:
    content = f.read()

content = content.replace(" gama.extension.serialize.fst,\n", "")
content = content.replace(" gama.extension.serialize.fst.annotations,\n", "")
content = content.replace(" gama.extension.serialize.fst.coders,\n", "")
content = content.replace(" gama.extension.serialize.fst.serializers,\n", "")
content = content.replace(" gama.extension.serialize.fst.util,\n", "")

with open('./gama.extension.serialize/META-INF/MANIFEST.MF', 'w') as f:
    f.write(content)
