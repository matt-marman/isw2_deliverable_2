package milestonetwo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader; 
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.opencsv.CSVWriter;

public class SortCSV {

public static boolean append = true;
public static ArrayList<String> aList = new ArrayList<String>();

public SortCSV(String csvPath) throws IOException {
	
	readAllLinesFromFile(csvPath);
    //System.out.println("Unsorted:\n");
    for(String aStudentString: aList){
        //System.out.println(aStudentString +"\n");
        break;
    }

}

public static ArrayList<String> readAllLinesFromFile(String path) throws IOException{

    FileReader fileReader = new FileReader(path);
    BufferedReader bufferedReader = new BufferedReader(fileReader);
    String line = null;
    while( (line = bufferedReader.readLine()) != null){
        aList.add(line);
    }
    
    bufferedReader.close();
    
    String[] release = {"4.0.0", "4.1.0", "4.1.1", "4.2.0", "4.2.1", "4.2.2", "4.3.0"};

    ArrayList<String> release1 = new ArrayList<String>();
    ArrayList<String> release2 = new ArrayList<String>();
    ArrayList<String> release3 = new ArrayList<String>();
    ArrayList<String> release4 = new ArrayList<String>();
    ArrayList<String> release5 = new ArrayList<String>();
    ArrayList<String> release6 = new ArrayList<String>();
    ArrayList<String> release7 = new ArrayList<String>();

    String feature = aList.get(0);
    
    for(int k = 1; k < aList.size(); k++) {
   
    	//System.out.print(aList.get(k).substring(0, 5) + "\n");
	    
	    	if(aList.get(k).substring(0, 5).equals(release[0])) 
	    		
	    		release1.add(aList.get(k));   		
	        		
    		if(aList.get(k).substring(0, 5).equals(release[1])) 
        		
        		release2.add(aList.get(k));   		
    		    	    		
    		if(aList.get(k).substring(0, 5).equals(release[2])) 
        		
        		release3.add(aList.get(k));   		
    				    	
			if(aList.get(k).substring(0, 5).equals(release[3])) 
        		
        		release4.add(aList.get(k));   		
		    				    				
			if(aList.get(k).substring(0, 5).equals(release[4])) 
        		
        		release5.add(aList.get(k));   		
        		
			if(aList.get(k).substring(0, 5).equals(release[5])) 
        		
        		release6.add(aList.get(k));   		

			if(aList.get(k).substring(0, 5).equals(release[6])) 
        		
        		release7.add(aList.get(k)); 			
    }
    
    //add each arraylist to the .csv file
    File file = new File(path);
	
    // create FileWriter object with file as parameter
	FileWriter outputfile = new FileWriter(file);

	// create CSVWriter object filewriter object as parameter
	CSVWriter writer = new CSVWriter(outputfile);

	// adding header to csv    
    String[] items = feature.split(",");
	writer.writeNext(items);

	// add data to csv
	for(int i = 0; i < release1.size(); i++) {
		
		
		String[] itemRow = release1.get(i).split(",");	
		writer.writeNext(itemRow);
	}
	
	// add data to csv
	for(int i = 0; i < release2.size(); i++) {
			
			
		String[] itemRow = release2.get(i).split(",");	
		writer.writeNext(itemRow);
	}
		
	// add data to csv
	for(int i = 0; i < release3.size(); i++) {
			
			
		String[] itemRow = release3.get(i).split(",");	
		writer.writeNext(itemRow);
	}
		
	// add data to csv
	for(int i = 0; i < release4.size(); i++) {
		
		
		String[] itemRow = release4.get(i).split(",");	
		writer.writeNext(itemRow);
	}
	
	// add data to csv
	for(int i = 0; i < release5.size(); i++) {
			
			
		String[] itemRow = release5.get(i).split(",");	
		writer.writeNext(itemRow);
	}
	
	for(int i = 0; i < release6.size(); i++) {
		
		
		String[] itemRow = release6.get(i).split(",");	
		writer.writeNext(itemRow);
	}
	
	for(int i = 0; i < release7.size(); i++) {
		
		
		String[] itemRow = release7.get(i).split(",");	
		writer.writeNext(itemRow);
	}
	
	// closing writer connection
	writer.close();
	
    return aList;
}
}
