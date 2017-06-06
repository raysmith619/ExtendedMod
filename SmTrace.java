package smTrace;

//package BlockWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * Facilitate execution traceing / logging of execution.
 * 
 * @author raysm
 *
 */
public class SmTrace {
	private static SmTrace traceObj;
	/**
	 * Create private constructor
	 */
	private SmTrace() {
		this.traceFlags = new HashMap<String, Integer>();
		// flags added as observed
		// create and load default properties
		this.defaultProps = new Properties();		
	}
	
	
	/**
	 * Setup access via singleton
	 */
	private static void init() {
		if (traceObj == null) {
			traceObj = new SmTrace();
		}
	}
	/**
	 * Shorthand
	 */
	public static boolean tr(String flag, int... levels) {
		init();		// Insure connection
		return traceObj.trace(flag,  levels);
	}
	
	
	/**
	 * Setup trace flags from string
	 * of the form flag1[=value][,flagN=valueN]*
	 * Flags are case-insensitive and must not contain ",".
	 * Values are optional and default to 1 if not present.
	 */
	public static void setFlags(String settings) {
		init();			// Insure access
		Pattern pat_flag_val = Pattern.compile("(\\w+)(=(\\d+),*)?");
		Matcher matcher = pat_flag_val.matcher(settings);
		while (matcher.find()) {
			String flag = matcher.group(1);
			String value = matcher.group(3);
			int val = 1;			// Default value if no =...
			if (value != null)
				val = Integer.parseInt(value);
			traceObj.setLevel(flag, val);
			int get_val = traceObj.getLevel(flag);
			System.out.println(String.format("flag=%s value=%d", flag, get_val));
		}
	}

	/**
	 * Set up based on properties file
	 * 
	 * @throws IOException
	 */
	public static void setProps(String propFile) {
		init();		// Setup singleton
		// Setup trace flags
		FileInputStream in;
		try {
			in = new FileInputStream(propFile);
		} catch (FileNotFoundException e) {
			File inf = new File(propFile);
			String abs_path;
			try {
				abs_path = inf.getCanonicalPath();
			} catch (IOException e1) {
				abs_path = "NO PATH for '" + propFile + "'";
				e1.printStackTrace();
			}

			System.err.println("Properties file " + abs_path + " not found");
			// e.printStackTrace();
			return;
		}
		try {
			traceObj.defaultProps.load(in);
		} catch (IOException e) {
			System.err.println("Can't load Properties file " + propFile);
			e.printStackTrace();
			return;
		}
		try {
			in.close();
		} catch (IOException e) {
			System.err.println("Can't load Properties file " + propFile);
			e.printStackTrace();
		}

	}

	public int getLevel(String trace_name) {
		Integer v = this.traceFlags.get(trace_name.toLowerCase());
		if (v == null)
			return 0;		// Not there == 0
		
		return (int)v;
	}
	
	public void setLevel(String trace_name) {
		setLevel(trace_name, 1);
	}

	public void setLevel(String trace_name, int level) {
		this.traceFlags.put(trace_name.toLowerCase(), level);
	}

	public boolean traceVerbose(int... levels) {
		return trace("verbose", levels);
	}

	/**
	 * @param debug
	 *            to set
	 */
	public void setDebug(int level) {
		setLevel("debug", level);
	}

	/**
	 * @param level
	 */
	public void setVerbose(int level) {
		setLevel("verbose", level);
	}

	/**
	 * @return the verbose
	 */
	public int getVerbose() {
		return getLevel("verbose");
	}

	/**
	 * Trace if at or above this level
	 */
	public boolean trace(String flag, int... levels) {
		int level = 1;
		if (levels.length > 0)
			level = levels[0];
		if (level < 1)
			return false;		// Don't even look

		return traceLevel(flag) >= level;
	}

	/**
	 * Return trace level
	 */
	public int traceLevel(String flag) {
		int traceLevel = getLevel(flag);
		return traceLevel;
	}
	
	
	/**
	 * 
	 * @param key
	 *            - property key
	 * @return property value, "" if none
	 */
	public String getProperty(String key) {
		return this.defaultProps.getProperty(key, "");
	}

	public void setProperty(String key, String value) {
		this.defaultProps.setProperty(key, value);
	}

	/**
	 * Get source absolute path Get absolute if fileName is not absolute TBD
	 * handle chain of paths like C include paths
	 * 
	 * @param fileName
	 * @return absolute file path
	 */
	public String getSourcePath(String fileName) {
		Path p = Paths.get(fileName);
		if (!p.isAbsolute()) {
			String[] dirs = getAsStringArray("source_files");
			ArrayList<String> searched = new ArrayList<String>();
			for (String dir : dirs) {
				File inf = new File(dir, fileName);
				if (inf.exists() && !inf.isDirectory()) {
					try {
						return inf.getCanonicalPath();
					} catch (IOException e) {
						System.err.printf("Problem with path %s,%s", dir, fileName);
					}
				}
				try {
					searched.add(inf.getCanonicalPath());
				} catch (IOException e) {
					// Ignore
				}
			}
			System.err.printf("%s was not found\n", fileName);
			if (dirs.length > 0) {
				System.err.printf("Searched in:\n");
				for (String dir : dirs) {
					File dirf = new File(dir);
					try {
						String dirpath = dirf.getCanonicalPath();
						System.err.printf("\t%s\n", dirpath);
					} catch (IOException e) {
						System.err.printf("\tpath error for %s\n", dir);
					}
				}
			}
			return fileName; // Return unchanged
		}
		return fileName; // Already absolute path
	}

	/**
	 * Get include absolute path Get absolute if fileName is not absolute TBD
	 * handle chain of paths like C include paths
	 * 
	 * @param fileName
	 * @return absolute file path, "" if not found
	 */
	public String getIncludePath(String fileName) {
		Path p = Paths.get(fileName);
		if (!p.isAbsolute()) {
			String[] dirs = getAsStringArray("include_files");
			ArrayList<String> searched = new ArrayList<String>();
			for (String dir : dirs) {
				File inf = new File(dir, fileName);
				if (inf.exists() && !inf.isDirectory()) {
					try {
						return inf.getCanonicalPath();
					} catch (IOException e) {
						System.err.printf("Problem with path %s,%s", dir, fileName);
					}
				}
				try {
					searched.add(inf.getCanonicalPath());
				} catch (IOException e) {
					// Ignore
				}
			}
			System.err.printf("%s was not found\n", fileName);
			if (dirs.length > 0) {
				System.err.printf("Searched in:\n");
				for (String dir : dirs) {
					File dirf = new File(dir);
					try {
						String dirpath = dirf.getCanonicalPath();
						System.err.printf("\t%s\n", dirpath);
					} catch (IOException e) {
						System.err.printf("\tpath error for %s\n", dir);
					}
				}
			}
			return ""; // Indicate as not found
		}
		return fileName; // Already absolute path
	}

	/**
	 * Get default properties key with value stored as comma-separated values as
	 * an array of those values If propKey not found, return an empty array
	 * 
	 * @param propKey
	 * @return array of string values
	 */
	public String[] getAsStringArray(String propKey) {
		String[] vals = getProperty(propKey).split(",");
		return vals;
	}

	private Properties defaultProps; // program properties
	private HashMap<String, Integer> traceFlags; // tracing flag/levels
}
