from lxml import etree
from glob import glob
import argparse


if __name__ == '__main__':
    # handle script arguments
    parser = argparse.ArgumentParser(description="Merges all found BugInstance in spotbugsXml.xml files found recursively in a directory into one file")
    parser.add_argument("--root_dir",  metavar='-r', default='.', help='The directory in which to run the script')
    parser.add_argument("--output_file_name", metavar='-o', default='merged_spotbugsXml.xml', help='The path of the resulting merged xml file')
    args = parser.parse_args()
    merged = None

    # we go through all the spotbugs result and add each of their element into the merged variable
    for xml in glob(args.root_dir + '/**/spotbugsXml.xml', recursive=True):
        file = open(xml, "r")

        # If it's the first file, we use it as our base file and will append other bugs into it
        if merged is None:
            merged = etree.parse(file).getroot()
        else:
            current = etree.parse(file).getroot()
            for elm in current:
                if elm.tag == "BugInstance":
                    merged.append(elm)

    # we save the merged result in a file
    out_content = etree.tostring(merged)
    out_file = open(args.output_file_name, "w")
    out_file.write(out_content.decode())
