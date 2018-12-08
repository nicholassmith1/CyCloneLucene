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

import cyclone.core.spi.CloneListener;
import cyclone.core.spi.CloneSearch;

public class LuceneReadIndexFromFile {

	public static void getMatches(CloneSearch cloneSearch,
			CloneListener listener) throws Exception {
		//Create lucene searcher. It search over a single IndexReader.
		IndexSearcher searcher = createSearcher();

		String searchText;
		StringBuffer buffer = new StringBuffer();

		try (Stream<String> lines = Files.lines(Paths.
				get(cloneSearch.target_file))) {
			int first_line = Integer.max((int)cloneSearch.start_line - 1, 0);

			Iterator<String> iter = lines.skip(first_line).iterator();
			String line;
			for (int i = first_line; i < cloneSearch.end_line
					&& iter.hasNext(); i++) {
				line = iter.next();
				buffer.append(line + "\n");
			}
		}
		searchText = buffer.toString();

		LuceneConstants.LOGGER.fine(searchText);

		//Search indexed contents using search term
		TopDocs foundDocs = searchInContent(searchText, searcher);

		//Total found documents
		LuceneConstants.LOGGER.info("Total Results :: " +
				foundDocs.totalHits);

		// Find max score, we'll use this as a crude normalization
		// mechanism
		float maxScore = 0;
		for (ScoreDoc sd : foundDocs.scoreDocs) {
			if (sd.score > maxScore) {
				maxScore = sd.score;
			}
		}

		for (ScoreDoc sd : foundDocs.scoreDocs) {
			Document d = searcher.doc(sd.doc);
			LuceneConstants.LOGGER.info("Path : "+ d.get("path") +
					", Score : " + sd.score);

			/* Filter out bad matches */
			if (sd.score < maxScore * LuceneConstants.REJECT_RATIO)
				continue;

			List<FindInFile.Search> results;
			results = FindInFile.find(new File(d.get("path")), searchText);

			for (FindInFile.Search search : results) {
				String path = new File(d.get("path")).toPath().
						toAbsolutePath().normalize().toString();
				
				// Reject clones of itself
				if (cloneSearch.target_file.equals(path) &&
						( ((cloneSearch.start_line >= search.startLine) &&
						 (cloneSearch.end_line <= search.endLine)) ||
						((search.startLine >= cloneSearch.start_line) &&
						 (search.endLine <= cloneSearch.end_line)) ) ) {
					continue;
				}
				
				LuceneConstants.LOGGER.info("DISCOVERED " +
						search.startLine +
						", " + search.endLine);
				// TODO fix time calculation
				listener.notifyCloneDetected(cloneSearch, path,
						search.startLine, search.endLine, sd.score,
						LuceneConstants.MODULE_NAME, 0);
			}
		}
	}

	private static TopDocs searchInContent(String textToFind,
			IndexSearcher searcher) throws Exception {
		//Create search query
		QueryParser qp = new QueryParser(Version.LUCENE_46, "contents",
				new StandardAnalyzer(Version.LUCENE_46));
		Query query = qp.parse(QueryParser.escape(textToFind));

		//search the index
		TopDocs hits = searcher.search(query, LuceneConstants.MAX_SEARCH);
		return hits;
	}

	private static IndexSearcher createSearcher() throws IOException {
		File file = new File(LuceneConstants.INDEX_DIR);
		file.mkdirs();
		Directory dir = FSDirectory.open(file);

		// It is an interface for accessing a point-in-time view of
		// a lucene index
		IndexReader reader = DirectoryReader.open(dir);

		//Index searcher
		IndexSearcher searcher = new IndexSearcher(reader);
		return searcher;
	}
}
