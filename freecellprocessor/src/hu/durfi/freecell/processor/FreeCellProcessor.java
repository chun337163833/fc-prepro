package hu.durfi.freecell.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.PrintWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

public class FreeCellProcessor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java -jar freecellprocessor.jar DIRECTORY");
			return;
		}
		
		String dirName = args[0];
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			
			File dir = new File(dirName);
			
			/*
			 * If param was a directory, do the parsing!
			 */
			if (dir.isDirectory()) {
				// New non-debugging handler.
				FreeCellXmlHandler handler = new FreeCellXmlHandler(false);
				
				File errorDir = new File(dir.getPath(), "error");
				
				// Write all pre-processed logs into one file? If this is false,
				// a new .csv file is generated for every .m3w file.
				// TODO: Set this with command line argument
				boolean oneFile = true;
				FileWriter oneWriter = new FileWriter(new File(dir, "output.csv"));
				
				File[] files = dir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".m3w");
					}
				});
				
				int numberOfFiles = files.length;
				
				for (int i = 0; i < files.length; i ++) {
					File inFile = files[i];
					System.out.println("Processing: " + inFile.getName() + " | " + (i+1) + "/" + numberOfFiles + ", (" + (((i+1) * 100) / numberOfFiles) + "%)");
					try {
						parser.parse(inFile, handler);
						FileWriter outFile = new FileWriter(inFile.getAbsolutePath() + ".csv");
						PrintWriter out = new PrintWriter(outFile);
						if (oneFile) {
							out = new PrintWriter(oneWriter);
						}
						
						while (!handler.processedGames.isEmpty()) {
							out.println(handler.processedGames.poll().toString());
						}
						
						out.close();
					} catch (Exception ex) {
						System.out.println("Error occured processing file: " + inFile.getName() + "! Moving file to \"error\" directory.");
						inFile.renameTo(new File(errorDir, inFile.getName()));
					}
				}
			/*
			 * If its a single file, debug that file!
			 */
			} else {
				// New debugging handler.
				FreeCellXmlHandler handler = new FreeCellXmlHandler(true);
				
				parser.parse(dir, handler);
				while (!handler.processedGames.isEmpty()) {
					System.out.println(handler.processedGames.poll().toString());
				}
			}
			
			// System.out.println("Results:");

		} catch (Exception ex) {
			System.out.println("Error parsing file: " + ex);
		} finally {
			System.out.println("Columns of CSV files:");
			System.out.println(ProcessedGameData.headerString());
		}
		

		
	}

}
