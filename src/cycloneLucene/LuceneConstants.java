package cycloneLucene;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LuceneConstants {
	public static final String MODULE_NAME = "LuceneExactMatch";

	public static final Logger LOGGER = Logger.getLogger(MODULE_NAME);
	public static final String CONTENTS = "contents";
	public static final String FILE_NAME = "filename";
	public static final String FILE_PATH = "filepath";
	public static final String INDEX_DIR = "/tmp/" + MODULE_NAME + "/indexedFiles";
	public static final int MAX_SEARCH = 1000;	// FIXME
	
	public static final double REJECT_RATIO = 0.5;
	
	static {
		Level level = Level.OFF;
		LOGGER.setLevel(level);
	}
}
