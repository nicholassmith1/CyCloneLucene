package cycloneLucene;

/**
 * Modified from https://howtodoinjava.com/lucene/lucene-index-and-search-text-files/
 */


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneReadIndexFromFile {
    //directory contains the lucene indexes
    private static final String INDEX_DIR = "indexedFiles";
 
    public static void main(String[] args) throws Exception
    {
        //Create lucene searcher. It search over a single IndexReader.
        IndexSearcher searcher = createSearcher();
         
        String searchText;
        StringBuffer buffer = new StringBuffer();
        
        String t_file = "/home/nism/Documents/git/CyCloneLucene/inputFiles/FuzzySearch.java";
        int t_start = 32;
        int t_end = 36;
        
		try (Stream<String> lines = Files.lines(Paths.get(t_file))) {
			int first_line = Integer.max(t_start - 1, 0);
		    
		    Iterator<String> iter = lines.skip(first_line).iterator();
		    String line;
		    for (int i = first_line; i < t_end && iter.hasNext(); i++) {
		    	line = iter.next();
			    buffer.append(line + "\n");
		    }
		}
		searchText = buffer.toString();
		
		System.out.println(searchText);
		System.out.println();
        
        //Search indexed contents using search term
//        TopDocs foundDocs = searchInContent("frequently", searcher);
        TopDocs foundDocs = searchInContent(searchText, searcher);
         
        //Total found documents
        System.out.println("Total Results :: " + foundDocs.totalHits);
         
        //Let's print out the path of files which have searched term
        for (ScoreDoc sd : foundDocs.scoreDocs)
        {
            Document d = searcher.doc(sd.doc);
            System.out.println("Path : "+ d.get("path") + ", Score : " + sd.score);
            
            if (sd.score < 1)
            	continue;
            
            List<FindInFile.Search> results;
            results = FindInFile.find(new File(d.get("path")), searchText);
            
            for (FindInFile.Search search : results) {
            	System.out.println("DISCOVERD " + search.startLine + ", " + search.endLine);
            }
            
        }
    }
     
    private static TopDocs searchInContent(String textToFind, IndexSearcher searcher) throws Exception
    {
        //Create search query
//        QueryParser qp = new QueryParser("contents", new StandardAnalyzer());
    	
//    	System.out.println(QueryParser.escape(textToFind));
    	
    	QueryParser qp = new QueryParser(Version.LUCENE_46, "contents", new StandardAnalyzer(Version.LUCENE_46));
        Query query = qp.parse(QueryParser.escape(textToFind));
         
        //search the index
//        searcher.sear
        TopDocs hits = searcher.search(query, 10);
        return hits;
    }
 
    private static IndexSearcher createSearcher() throws IOException
    {
//        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
    	Directory dir = FSDirectory.open(new File(INDEX_DIR));
         
        //It is an interface for accessing a point-in-time view of a lucene index
        IndexReader reader = DirectoryReader.open(dir);
         
        //Index searcher
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }
}
