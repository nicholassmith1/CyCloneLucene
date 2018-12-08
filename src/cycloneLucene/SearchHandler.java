package cycloneLucene;

import cyclone.core.spi.CloneListener;
import cyclone.core.spi.CloneSearch;
import cyclone.core.spi.CloneSearchStatusListener;

public class SearchHandler implements Runnable {

	private CloneSearch cloneSearch;
	private CloneListener listener;
	private CloneSearchStatusListener statusListener;
	
	public SearchHandler(CloneSearch cloneSearch, CloneListener listener,
			CloneSearchStatusListener statusListener) {
		this.cloneSearch = cloneSearch;
		this.listener = listener;
		this.statusListener = statusListener;
	}
	
	@Override
	public void run() {
		LuceneConstants.LOGGER.info("Starting search for " +
				cloneSearch.target_file + ":" + cloneSearch.start_line +
				"," + cloneSearch.end_line);

		synchronized (SearchHandler.class) {
			for (String path : cloneSearch.source_files) {
				LuceneWriteIndexFromFile.indexPath(path);
			}

			try {
				LuceneReadIndexFromFile.getMatches(cloneSearch, listener);
			} catch (Exception e) {
				;
			} finally {
				statusListener.notifyComplete(cloneSearch);
			}
		}
	}

}
