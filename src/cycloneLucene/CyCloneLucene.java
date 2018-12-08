package cycloneLucene;

import java.util.WeakHashMap;

import cyclone.core.spi.CloneDetectorService;
import cyclone.core.spi.CloneListener;
import cyclone.core.spi.CloneSearch;
import cyclone.core.spi.CloneSearchStatusListener;

public class CyCloneLucene implements CloneDetectorService {
	
	private static final String[] SUPPORTED_EXTENSIONS = {
		"c", "java", "xml", "m", "py", "cpp", "h", "hpp",
		"php"
	};
	private WeakHashMap<CloneSearch, Thread> currentSearches =
			new WeakHashMap<>();

	@Override
	public void cancel(CloneSearch search) {
		Thread thread = currentSearches.get(search);
		if (thread != null) {
			thread.interrupt();
		}
	}

	@Override
	public String[] getSupportedExtensions() {
		return SUPPORTED_EXTENSIONS;
	}

	@Override
	public void search(CloneSearch cloneSearch, CloneListener listener,
			CloneSearchStatusListener statusListener) {
		SearchHandler handler = new SearchHandler(cloneSearch,
				listener, statusListener);
		
		Thread thread = new Thread(handler);
		currentSearches.put(cloneSearch, thread);
		thread.start();
	}

	@Override
	public void updateCache(String arg0) {
		// Nothing to do here
		;
	}

}
