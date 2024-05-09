from lxml import etree
import argparse


def identical_nodes(node1: etree.Element, node2: etree.Element):
    if node1.tag == node2.tag:
        for name, value in node1.items():
            if not ((name, value) in node2.items()):
                return False
        for subNode in node1:
            found = False
            for subNode2 in node2:
                if identical_nodes(subNode, subNode2):
                    found = True
                    break
            if not found:
                return False
        return True
    return False

def is_gaml_additions(node):
    clazz = node.find("Class")
    return clazz is not None and "GamlAdditions" in clazz.get("classname", "")

if __name__ == '__main__':

    parser = argparse.ArgumentParser(description="Creates an xml file that contains the BugInstances that are only specific to one of the two spotbugs xml result file given as a parameter. It is used to get a 'diff' to see if there's improvement between two compilations.")
    parser.add_argument('-n', "--new_file", help='The path of the most recent file to compare')
    parser.add_argument('-o', "--old_file", help='The path of the oldest file to compare')
    parser.add_argument('-d', "--diff_file", help='The path of the output diff file')
    args = parser.parse_args()

    new_root = etree.parse(open(args.new_file)).getroot()
    old_root = etree.parse(open(args.old_file)).getroot()
    for element in new_root.findall("BugInstance"):
        # If it's in GamlAdditions we ignore that bug
        if is_gaml_additions(element):
            new_root.remove(element)
        else: # Else we check if it already existed before, in which case it's not new and we also ignore it
            for element2 in old_root:
                if identical_nodes(element, element2):
                    new_root.remove(element)
                    break
    out_content = etree.tostring(new_root)
    out_file = open(args.diff_file, "w")
    out_file.write(out_content.decode())
    