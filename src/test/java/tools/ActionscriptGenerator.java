package tools;

import static utils.FindFiles.findFiles;
import static utils.FindFiles.getFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.flagstone.translate.Profile;

public class ActionscriptGenerator {

    private static final String RESOURCE_DIR =
    	"src/test/resources/actionscript/models";
    private static final String DEST_DIR = "resources";

    private static final String PROFILES = "profiles";
    private static final String REFID = "refid";
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
		findFiles(files, srcDir, getFilter(".yaml"));

		FileInputStream stream = null;

		for (String yamlFile : files) {
			try {
		        stream = new FileInputStream(yamlFile);
		        for (Object tests : (List<Object>)yaml.load(stream)) {
		        	generateTest((Map<String,Object>) tests);
		        }
		        stream.close();
			} catch (Exception e) {
				System.err.println(yamlFile + ": " + e.getMessage());
			}
		}
    }

    @SuppressWarnings("unchecked")
    private static void generateTest(final Map<String,Object>list)
    		throws IOException {
        String script = (String)list.get(SCRIPT);
        List<String> profiles = (List<String>)list.get(PROFILES);
        List<Object> parameters = (List<Object>)list.get(PARAMETERS);

        String path;
        String type;

        if (list.containsKey(FILE)) {
        	path = (String)list.get(FILE);
        } else if (list.containsKey(REFID)) {
        	path = (String)list.get(REFID) + ".as";
        } else {
        	throw new IllegalArgumentException("No file specified");
        }

        if (script.startsWith("onClipEvent")) {
        	type = "button";
        } else if (script.startsWith("on")) {
        	type = "event";
        } else {
        	type = "frame";
        }

        File dir;
        File file;
        int index;

        Map<String, Object> vars;

        for (String profile : profiles) {
        	dir = dirForTest(profile, type);
	        index = 0;

        	if (parameters == null) {
            	writeScript(new File(dir, path), script);
        	} else {
            	for (Object set : parameters) {
                	vars = (Map<String, Object>)set;
                	if (vars.containsKey(IGNORE)) {
                		continue;
                	}
            		if (vars.containsKey(FILE)) {
	                	file = new File(dir, (String)vars.get(FILE));
            		} else if (vars.containsKey(REFID)) {
	                	file = new File(dir, (String)vars.get(REFID) + ".as");
            		} else {
				        file = new File(dir, String.format(path, index++));
            		}
                	script = replaceTokens(vars, script);
                	writeScript(file, script);
            	}
        	}
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
