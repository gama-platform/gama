import os
import glob

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # If the file contains TypeHandler, skip it (already fixed EclipseIndividualSerialiser)
    if "implements TypeHandler" in content:
        return

    # Look for the class declaration to add generic type
    import re
    match = re.search(r'class \w+ extends EclipseIndividualSerialiser<([^>]+)>', content)
    if match:
        type_param = match.group(1)
        # Update deserialise method signature
        content = re.sub(r'public ' + type_param + r' deserialise\(final IScope scope, final IGamaObjectInput in\)',
                         r'@Override\n\tpublic ' + type_param + r' deserialise(final IScope scope, final IGamaObjectInput in)', content)
        # Handle the case where they used "public Object deserialise"
        content = re.sub(r'public Object deserialise\(final IScope scope, final IGamaObjectInput in\)',
                         r'@Override\n\tpublic ' + type_param + r' deserialise(final IScope scope, final IGamaObjectInput in)', content)

        with open(filepath, 'w') as f:
            f.write(content)

for filepath in glob.glob('./gama.extension.serialize/src/gama/extension/serialize/binary/*.java'):
    fix_file(filepath)
