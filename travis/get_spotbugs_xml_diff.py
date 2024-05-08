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


if __name__ == '__main__':

    parser = argparse.ArgumentParser(description="Creates an xml file that contains the BugInstances that are only specific to one of the two spotbugs xml result file given as a parameter. It is used to get a 'diff' to see if there's improvement between two compilations.")
    parser.add_argument('-n', "--new_file", help='The path of the most recent file to compare')
    parser.add_argument('-o', "--old_file", help='The path of the oldest file to compare')
    parser.add_argument('-d', "--diff_file", help='The path of the output diff file')
    args = parser.parse_args()

    new_root = etree.parse(open(args.new_file)).getroot()
    old_root = etree.parse(open(args.old_file)).getroot()
    out_root = etree.Element("BugCollection")
    for element in new_root:
        if element.tag == "BugInstance":
            found = False
            for element2 in old_root:
                if identical_nodes(element, element2):
                    found = True
                    break
            if not found:
                out_root.append(element)
    out_content = etree.tostring(out_root)
    out_file = open(args.diff_file, "w")
    out_file.write(out_content.decode())
    