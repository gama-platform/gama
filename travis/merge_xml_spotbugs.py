from lxml import etree
from glob import glob
import argparse


if __name__ == '__main__':
    # handle script arguments
    parser = argparse.ArgumentParser(description="Merges all found BugInstance in spotbugsXml.xml files found recursively in a directory into one file")
    parser.add_argument('-r', "--root_dir", default='.', help='The directory in which to run the script')
    parser.add_argument('-o', "--output_file_name", default='merged_spotbugsXml.xml', help='The path of the resulting merged xml file')
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
            # merge SrcDir
            project = merged.find("Project")
            for srcdir in current.find("Project").findall("SrcDir"):
                project.append(srcdir)
            # merge all buginstance
            for elm in current.findall("BugInstance"):
                merged.append(elm)
            # merge bugpatterns as well
            for elm in current.findall("BugPattern"):
                merged.append(elm)

    # we save the merged result in a file
    out_content = etree.tostring(merged)
    out_file = open(args.output_file_name, "w")
    out_file.write(out_content.decode())
