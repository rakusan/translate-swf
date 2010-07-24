package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class ActionscriptGenerator {

    private static final String RESOURCE_DIR = "src/test/resources/actionscript";
    private static final String DEST_DIR = "resources";

    private static final String NAME = "name";
    private static final String PROFILES = "profiles";
    private static final String TYPE = "type";
    private static final String VALUES = "values";
    private static final String SCRIPT = "script";

    @SuppressWarnings("unchecked")
    public static void main(final String[] args) {

		final File srcDir;

		if (System.getProperty("test.suite") == null) {
			srcDir = new File(RESOURCE_DIR);
		} else {
			srcDir = new File(System.getProperty("test.suite"));
		}

		Yaml yaml = new Yaml();
		List<String> files = new ArrayList<String>();
		findFiles(files, srcDir, getFilter());

		FileInputStream stream = null;
		int index;

		Map<String,Object>test;
		String name;
		String script;
		String type;
	    List<String>profiles;
	    List<Object>values;
	    Map<String, Object> vars;

	    File dir;
	    File file;

		try {
			for (String yamlFile : files) {
		        stream = new FileInputStream(yamlFile);

		        for (Object tests : (List<Object>)yaml.load(stream)) {
	                test = (Map<String,Object>)tests;
			        index = 0;

	                name = (String)test.get(NAME);
	                type = (String)test.get(TYPE);
	                script = (String)test.get(SCRIPT);
	                profiles = (List<String>)test.get(PROFILES);
                	values = (List<Object>)test.get(VALUES);

	                for (String profile : profiles) {
	                	dir = dirForTest(profile, type);
	                	for (Object set : values) {
		                	vars = (Map<String, Object>)set;
		                	file = fileForTest(dir, name, index++);
		                	script = replaceTokens(vars, script);
		                	writeScript(file, script);
	                	}
	                }
		        }
		        stream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    private static File dirForTest(String profile, String type)
    		throws IOException {
    	String[] items = profile.split(":");
		String path = String.format(
				"as%s/swf%s/%s/%s", items[0], items[1], items[2], type);
		File dir = new File(DEST_DIR, path);

		if (!dir.exists() && !dir.mkdir()) {
			throw new IOException("Cannot create directory: " + dir.getPath());
		}
		return dir;
    }

    private static File fileForTest(File dir, String prefix, int number) {
		String name = String.format("%s_%d.as", prefix, number);
		return new File(dir, name);
    }

    private static FilenameFilter getFilter() {
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				boolean accept = false;

				File file = new File(dir, name);

				if (name.endsWith(".yaml")) {
					accept = true;
				} else if (file.isDirectory() && name.equals("..") == false
						&& name.equals(".") == false) {
					accept = true;
				}
				return accept;
			}
		};
    }

	private static void findFiles(List<String> list, File directory,
			FilenameFilter filter) {
		String[] files = directory.list(filter);

		for (int i = 0; i < files.length; i++) {
			File file = new File(directory, files[i]);

			if (file.isDirectory()) {
				findFiles(list, file, filter);
			} else {
				list.add(file.getPath());
			}
		}
	}

	private static String replaceTokens(final Map<String,Object>values,
			final String script) {
		String str = script;
    	for (Map.Entry<String, Object> set: values.entrySet()) {
    		str = str.replaceAll("%"+set.getKey()+"%",
    				set.getValue().toString());
    	}
    	return str;
	}

	private static void writeScript(final File file, final String script)
	   		throws IOException {
		PrintWriter writer = new PrintWriter(file);
		writer.write(script);
		writer.flush();
		writer.close();
	}

}
