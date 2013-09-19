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
		if (args.length != 2) {
			System.out.println("Usage: java -jar freecellprocessor.jar [json/xml] [DIRECTORY]");
			return;
		}
		boolean json = false;
		if (args[0].equalsIgnoreCase("json")) {
			json = true;
		}
		
		String dirName = args[1];
		File dir = new File(dirName);
		
		try {
			/*
			 * If param was a directory, do the parsing!
			 */
			if (dir.isDirectory()) {
				if (!json) {
					// XML part
					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser parser = factory.newSAXParser();
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
				} else {
					// JSON part

					FreeCellJsonHandler handler = new FreeCellJsonHandler(false);
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
							handler.parseFile(inFile);
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
							ex.printStackTrace();
							inFile.renameTo(new File(errorDir, inFile.getName()));
						}
					}
				}
			} 
			/*
			 * If its a single file, debug that file!
			 */
			else {
				if (!json) {
					// XML part
					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser parser = factory.newSAXParser();
					// New debugging handler.
					FreeCellXmlHandler handler = new FreeCellXmlHandler(true);
					
					parser.parse(dir, handler);
					while (!handler.processedGames.isEmpty()) {
						System.out.println(handler.processedGames.poll().toString());
					}
				} else {
					// JSON part
					FreeCellJsonHandler handler = new FreeCellJsonHandler(true);
					handler.parseFile(dir);

					while (!handler.processedGames.isEmpty()) {
						System.out.println(handler.processedGames.poll().toString());
					}
				}
			}
		} catch (Exception ex) {
			System.out.println("Error parsing file: " + ex);
		} finally {
			System.out.println("Columns of CSV files:");
			System.out.println(ProcessedGameData.headerString());
		}
		
	}

}
