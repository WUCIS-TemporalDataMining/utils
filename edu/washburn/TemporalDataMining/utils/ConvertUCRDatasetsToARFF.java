package edu.washburn.TemporalDataMining.utils;
/** This class converts all UCR time series datasets from their Matlab format to
*	ARFF format in order to be used within Weka.
*	
*	@author Jared Ready
*	@date February 28th, 2015
*/

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.MatlabLoader;
 
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ConvertUCRDatasetsToARFF {
	
	public static void main(String [] args) throws Exception {	
		if(args.length != 1) {
			System.out.println("\nUsage: ConvertUCRDatasetsToARFF <directory>");
			System.exit(1);
		}

		File newBaseDirectory = new File("UCR_ARFF");
		if(!newBaseDirectory.exists()) {
			try {
				newBaseDirectory.mkdir();
			} catch(SecurityException se) {
				System.out.println(se.getMessage());
			}
		}
		else {
			System.out.println("\nPlease delete/move directory $PWD/UCR_ARFF and rerun.");
			System.exit(1);
		}
		
		ArrayList<File> datasets = new ArrayList<File>();
		getFiles(args[0], datasets);
		for(File dataset : datasets) {
			String[] partsOfPath = dataset.getCanonicalPath().split("/");
			String currentDirectory = partsOfPath[partsOfPath.length - 1];
			File subDirectory = new File(newBaseDirectory, currentDirectory);
			if(!subDirectory.exists()) {
				subDirectory.mkdir();
			}
			MatlabLoader loader = new MatlabLoader();
			loader.setSource(dataset);
			Instances data = loader.getDataSet();
			
			ArffSaver saver = new ArffSaver();
		    saver.setInstances(data);
		    saver.setFile(new File(subDirectory, dataset.getName() + ".arff"));
		    saver.setDestination(new File(subDirectory, dataset.getName() + ".arff"));
		    saver.writeBatch();
		}
	}

	public static void getFiles(String directoryName, ArrayList<File> files) {
	    File directory = new File(directoryName);

	    // get all the files from a directory
	    ArrayList<File> fileList = new ArrayList<File>(Arrays.asList(directory.listFiles()));
	    for (File file : fileList) {
	        if (file.isFile()) {
	            files.add(file);
	        } else if (file.isDirectory()) {
	        	getFiles(file.getAbsolutePath(), files);
	        }
	    }
	}
}