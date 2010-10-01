package tools;

import static utils.FindFiles.findFiles;
import static utils.FindFiles.getFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class TestCoverageReport {

    private static final String REFERENCE_DIR =
    	"src/test/resources/actionscript/reference";
    private static final String SUITE_DIR =
    	"src/test/resources/actionscript/models";

    private static final String IDENTIFIER = "id";
    private static final String REFID = "refid";
    private static final String ELEMENTS = "elements";

    private static final Map<String, Boolean> identifiers =
    	new LinkedHashMap<String, Boolean>();

    public static void main(final String[] args) {
    	final String out;

    	if (args.length == 1) {
    		out = args[0];
    	} else {
    		out = "target/test-coverage.txt";
    	}

		loadIdentifiers(identifiers, new File(REFERENCE_DIR));
		checkReferences(identifiers, new File(SUITE_DIR));

		try {
			PrintWriter writer = new PrintWriter(out);
			writer.println("No tests defined for the following references:");
			for (Map.Entry<String, Boolean> entry : identifiers.entrySet()) {
				if (!entry.getValue()) {
					writer.println(entry.getKey());
				}
			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			System.err.println("Cannot write to file: " + out);
		}
    }

    @SuppressWarnings("unchecked")
    private static void loadIdentifiers(final Map<String, Boolean> table,
    		final File dir) {
		Yaml yaml = new Yaml();
		List<String> files = new ArrayList<String>();
		findFiles(files, dir, getFilter(".yaml"));

		FileInputStream stream = null;
		Map<String,Object> map;

		for (String yamlFile : files) {
			try {
		        stream = new FileInputStream(yamlFile);
		        for (Object entry : (List<Object>)yaml.load(stream)) {
		        	map = (Map<String,Object>) entry;
		        	if (map.containsKey(ELEMENTS)) {
		        		for (Object element : (List<Object>) map.get(ELEMENTS)) {
			        		loadProfile(table, (Map<String,Object>) element);
		        		}
		        	} else {
		        		loadProfile(table, (Map<String,Object>) entry);
		        	}
		        }
		        stream.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(yamlFile + ": " + e.getMessage());
			}
		}
    }

    private static void loadProfile(final Map<String, Boolean> identifiers,
    		final Map<String,Object> entry) {
    	if (!entry.containsKey(IDENTIFIER)) {
    		throw new IllegalArgumentException(
    				"Missing identifier: " + entry.toString());
    	}

    	identifiers.put((String)entry.get(IDENTIFIER), false);
    }

    @SuppressWarnings("unchecked")
    private static void checkReferences(final Map<String, Boolean> table,
    		final File dir) {
		Yaml yaml = new Yaml();
		List<String> files = new ArrayList<String>();
		findFiles(files, dir, getFilter(".yaml"));

		FileInputStream stream = null;
		Map<String,Object> map;
		String reference;

		for (String yamlFile : files) {
			try {
		        stream = new FileInputStream(yamlFile);
		        for (Object entry : (List<Object>)yaml.load(stream)) {
		        	map = (Map<String,Object>) entry;
		        	if (map.containsKey(REFID)) {
			        	reference = (String) map.get(REFID);
		        		if (table.containsKey(reference)) {
		        			table.put(reference, true);
		        		} else {
		        			System.err.println("Undocumented feature: " + reference);
		        		}
		        	}
		        }
		        stream.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(yamlFile + ": " + e.getMessage());
			}
		}
    }

}
