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

import com.flagstone.translate.Profile;

public class ActionscriptGenerator {

    private static final String RESOURCE_DIR = "src/test/resources/actionscript";
    private static final String DEST_DIR = "resources";

    private static final String PROFILES = "profiles";
    private static final String TYPE = "type";
    private static final String PARAMETERS = "parameters";
    private static final String SCRIPT = "script";
    private static final String IGNORE = "ignore";
    private static final String FILE = "file";

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
		String script;
		String type;
	    List<String>profiles;
	    List<Object>parameters;
	    Map<String, Object> vars;

	    File dir;
	    File file;

		try {
			for (String yamlFile : files) {
		        stream = new FileInputStream(yamlFile);

		        for (Object tests : (List<Object>)yaml.load(stream)) {
	                test = (Map<String,Object>)tests;
			        index = 0;

	                type = (String)test.get(TYPE);
	                script = (String)test.get(SCRIPT);
	                profiles = (List<String>)test.get(PROFILES);
                	parameters = (List<Object>)test.get(PARAMETERS);

	                for (String profile : profiles) {
	                	dir = dirForTest(profile, type);
	                	for (Object set : parameters) {
		                	vars = (Map<String, Object>)set;
		                	if (!vars.containsKey(IGNORE)) {
			                	file = new File(dir, vars.get(FILE).toString());
			                	script = replaceTokens(vars, script);
			                	writeScript(file, script);
		                	}
	                	}
	                }
		        }
		        stream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    private static File dirForTest(String name, String type)
    		throws IOException {
    	Profile profile = Profile.fromName(name);
		String path = String.format(
				"as%d/swf%d/%s/%s", profile.getScriptVersion(),
				profile.getFlashVersion(), profile.getPlayer(), type);
		File dir = new File(DEST_DIR, path);

		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Cannot create directory: " + dir.getPath());
		}
		return dir;
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
		File dir = file.getParentFile();

		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Cannot create directory: " + dir.getPath());
		}
		PrintWriter writer = new PrintWriter(file);
		writer.write(script);
		writer.flush();
		writer.close();
	}

}
