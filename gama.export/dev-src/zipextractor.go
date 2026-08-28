package main

import 	(
	"fmt"
	"archive/zip"
	"path/filepath"
	"os"
	"io"
)

func main() {
	var err error
	var outputDirPath string = ""
	var inputZipPath string = ""

	var argument string
	var verbose bool = false
	var missingArguments bool = true
	
	// parse arguments
	for _, argument = range os.Args[1:] {
		if argument == "-v" {
			verbose = true
			continue
		}

		if inputZipPath == "" {
			inputZipPath = argument
		} else {
			outputDirPath = argument
			missingArguments = false
		}
	}

	if missingArguments {
		fmt.Println(filepath.Base("usage: " + os.Args[0]) +  " [-v] <source_file> <destination_dir>")
		os.Exit(1)
	}

	if verbose {
		fmt.Println("source : " + inputZipPath)
		fmt.Println("target : " + outputDirPath)
	}

	if _, err = os.Stat(inputZipPath); err != nil {
		fmt.Println("error : the source zip file " + inputZipPath + "does not exist.")
		os.Exit(1)
	}

	if _, err = os.Stat(outputDirPath); err != nil {
		fmt.Println("error : the destination directory " + outputDirPath + "does not exist.")
		os.Exit(1)
	}

	var reader  *zip.ReadCloser 

	reader, err = zip.OpenReader(inputZipPath)
	
	if err != nil {
		panic(err)
	}
	
	defer reader.Close()
	
	var sourceFile *zip.File
	var targetFile *os.File
	var sourceFileContentReadCloser io.ReadCloser
	var targetPath string
	
	for _, sourceFile = range reader.File {
		targetPath = filepath.Join(outputDirPath,filepath.Clean(sourceFile.Name))
		
		if sourceFile.FileInfo().IsDir() {
			if err = os.MkdirAll(targetPath, sourceFile.Mode()); err != nil {
				panic(err)
			}
			continue
		}

		// Ensure the parent directory exists for files nested in folders
		if err = os.MkdirAll(filepath.Dir(targetPath), 0755); err != nil {
			panic(err)
		}

		// Open the archive file, to read its content
		sourceFileContentReadCloser, err = sourceFile.Open()
		if err != nil {
			panic(err)
		}

		// Create the target file, preserve unix permissions
		targetFile, err = os.OpenFile(
			targetPath,
			os.O_WRONLY | os.O_CREATE | os.O_TRUNC,
			sourceFile.Mode(),
		)
		
		if err != nil {
			panic(err)
		}

		_, err = io.Copy(targetFile, sourceFileContentReadCloser)

		if err != nil {
			panic(err)
		}		

		targetFile.Close()
		sourceFileContentReadCloser.Close()
	}

	os.Exit(0)
}