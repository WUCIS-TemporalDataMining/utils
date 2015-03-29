package edu.washburn.TemporalDataMining.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;

public class MirrorUCRDatasets {

	public static void main(String[] args) throws Exception {
		if(args.length != 1) {
			System.out.println("\nUsage: MirrorUCRDatasets <directory>");
			System.exit(1);
		}
		
		File newBaseDirectory = new File("UCR_ARFF_Mirrored");
		if(!newBaseDirectory.exists()) {
			try {
				newBaseDirectory.mkdir();
			} catch(SecurityException se) {
				System.out.println(se.getMessage());
			}
		}
		else {
			System.out.println("\nPlease delete/move directory $PWD/UCR_ARFF_Mirrored and rerun.");
			System.exit(1);
		}
		
		ArrayList<File> datasets = new ArrayList<File>();
		getFiles(args[0], datasets);
		for(File dataset : datasets) {
			String[] partsOfPath = dataset.getCanonicalPath().split("/");
			String currentDirectory = partsOfPath[partsOfPath.length - 2];
			File subDirectory = new File(newBaseDirectory, currentDirectory);
			if(!subDirectory.exists()) {
				subDirectory.mkdir();
			}
			
			ArffLoader loader = new ArffLoader();
			loader.setSource(dataset);
			Instances data = loader.getDataSet();
			data.setClassIndex(0);
			
			int powerOfTwo = 1;
			// -1 because we don't want to consider class attribute
			while(powerOfTwo < data.numAttributes() - 1) {
				powerOfTwo <<= 1;
			}
			
			int originalNumAttributes = data.numAttributes() - 1;
			// Add attributes until numAttributes() = power of two
			for(int i = 0; data.numAttributes() - 1 < powerOfTwo; i++) {
				data.insertAttributeAt(new Attribute("attr_mirror_" + i), data.numAttributes());
			}
			
			for(int i = 0; i < data.numInstances(); i++) {
				double classValue = data.get(i).classValue();
				// Need to take all attributes - class attribute, where class attribute is index 0
				double[] mirroredInstance = mirrorToPowerOfTwo(Arrays.copyOfRange(data.get(i).toDoubleArray(), 1, originalNumAttributes));
				double[] mirroredInstanceWithClassAttr = new double[mirroredInstance.length + 1];
				mirroredInstanceWithClassAttr[0] = classValue;
				for(int ii = 1; ii < mirroredInstanceWithClassAttr.length; ii++) {
					mirroredInstanceWithClassAttr[ii] = mirroredInstance[ii - 1];
				}
				
				for(int ii = 1; ii < mirroredInstanceWithClassAttr.length; ii++) {
					data.instance(i).setValue(ii, mirroredInstanceWithClassAttr[ii]);
				}
			}
			
			ArffSaver saver = new ArffSaver();
		    saver.setInstances(data);
		    saver.setFile(new File(subDirectory, dataset.getName() + ".arff"));
		    saver.setDestination(new File(subDirectory, dataset.getName() + ".arff"));
		    saver.writeBatch();
		}

	}
	
	public static double[] mirrorToPowerOfTwo(double[] input) {
		// If input.length is 0, what are you even doing here?
		if(input.length == 0) {
			return input;
		}
		// Checks if input.length is power of two
		// Refer, http://en.wikipedia.org/wiki/Power_of_two#Fast_algorithm_to_check_if_a_positive_number_is_a_power_of_two
		if((input.length & (input.length - 1)) == 0) {
			return input;
		}
		//Need to get the next power of two, that is the next integer n > input.length such that n = 2^k for some positive
		//integer k
		int powerOfTwo = 1;
		while(powerOfTwo < input.length) {
			powerOfTwo <<= 1;
		}
		double[] output = new double[powerOfTwo];
		System.arraycopy(input, 0, output, 0, input.length);
		//Now we can mirror
		for(int i = input.length, j = input.length - 1; i < powerOfTwo; i++, j--) {
			output[i] = input[j];
		}
		
		return output;
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
