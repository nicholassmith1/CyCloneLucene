package cycloneLucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FindInFile {
	
	public static class Search {
		public boolean discovered;
		int startLine;
		int endLine;
		
		public Search() {
			this(false, 0, 0);
		}
		
		public Search(boolean discovered, int startLine, int endLine) {
			this.discovered = discovered;
			this.startLine = startLine;
			this.endLine = endLine;
		}
	}
	
	private static String sterilizeLine(String line) {
		return line.replaceAll("[\\s\\{\\},.\\[\\]\\(\\)]", "");
	}
	
	private static boolean linesMatch(String l0, String l1) {
		return sterilizeLine(l0).equals(sterilizeLine(l1));
	}
	
	public static List<Search> find(File file, String pattern) throws IOException {
		List<Search> results = new ArrayList<>();
		int startLine = 1;
		int endLine = 1;
		
		/* divide input up by line returns */
		String[] patternLines = pattern.split("\\r?\\n");
		
		/* removed empty lines. */
		List<String> sterilizedLines = new ArrayList<>();
		for (String s : Arrays.asList(patternLines)) {
			String sl = sterilizeLine(s); 
			if (!sl.equals("")) {
				sterilizedLines.add(s);
			}
		}
		
		if (sterilizedLines.isEmpty())
			return results;
		
		/*
		 * Traverse the entire file, line-by-line. If we find that
		 * any line matches, iterate through the remaining lines
		 * in the pattern, matching sure each of them matches.
		 * 
		 * Some special effort has to be made to handle possible
		 * white space variances.
		 */
		Stream<String> lines = Files.lines(file.toPath());
		try {			
			Iterator<String> i0 = lines.iterator();
			while (i0.hasNext()) {
				String s0 = i0.next();
				
				/* Skip empty lines */
				if (sterilizeLine(s0).equals("")) {
					startLine++;
					continue;
				}
				
				LuceneConstants.LOGGER.finer("checking " + startLine + "\"" +
						sterilizeLine(s0) + "\" :: \"" +
						sterilizeLine(sterilizedLines.get(0)) + "\"");
				
				/* check if lines match, stripping out which characters */
				if (linesMatch(s0, sterilizedLines.get(0))) {
					Stream<String> lines2 = Files.lines(file.toPath());
					
					Iterator<String> i1 = lines2.skip(startLine - 1).iterator();
					Iterator<String> i2 = sterilizedLines.iterator();
					boolean match = true;
					endLine = startLine;
					while (i2.hasNext() && i1.hasNext()) {
						String s1 = i2.next();
						String s2 = i1.next();
						
						/* Skip empty lines */
						if (sterilizeLine(s2).equals("")) {
							endLine++;
							if (i1.hasNext()) {
								s2 = i1.next();
							} else {
								continue;
							}
						}
						
						LuceneConstants.LOGGER.finer("checking2 " + s1 + " :: " + s2);
						
						if (!linesMatch(s1, s2)) {
							LuceneConstants.LOGGER.finer("EXIT");
							
							match = false;
							break;
						}
						endLine++;
					}
					
					lines2.close();
					if (match) {
						lines2.close();
						results.add(new Search(true, startLine, endLine));
					}
				}
				startLine++;
			}
		} finally {
			lines.close();
		}
		
		
		return results;
	}
	
	
}
