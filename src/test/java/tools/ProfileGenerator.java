package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.flagstone.translate.Profile;

public class ProfileGenerator {

    private static final String RESOURCE_DIR = "src/test/resources/profiles";
    private static final String DEST_DIR = "resources";

    private static final String ACTIONSCRIPT = "actionscript";
    private static final String FLASH = "flash";
    private static final String PLAYER = "player";
    private static final String TYPE = "type";

    private static final String[] TYPES = { "frame", "button", "event" };

    @SuppressWarnings("unchecked")
    public static void main(final String[] args) {
		Profile profile;
		String publish;
		String script;
		File jsFile;

	    File dir;
	    File file;

		try {
			File yamlFile = new File(RESOURCE_DIR, "profiles.yaml");
			File publishFile = new File(RESOURCE_DIR, "publish.xml");
			FileInputStream stream = new FileInputStream(yamlFile);
			Yaml yaml = new Yaml();
			Map<String,Object>map = new LinkedHashMap<String, Object>();

	        for (Object list : (List<Object>)yaml.load(stream)) {
                profile = Profile.fromName(list.toString());
                map.put(ACTIONSCRIPT, profile.getScriptVersion());
                map.put(FLASH, profile.getFlashVersion());
                map.put(PLAYER, profile.getPlayer());

                publish = contentsOfFile(publishFile);
            	publish = replaceTokens(map, publish);

        		jsFile = new File(RESOURCE_DIR, "publish.jsfl");
        		script = contentsOfFile(jsFile);

            	for (String type : TYPES) {
            		map.put(TYPE, type);

            		dir = dirForTest(map);

            		file = new File(dir, "publish.xml");
            		writeScript(file, publish);

            		file = new File(dir, type+".jsfl");
            		writeScript(file, replaceTokens(map, script));

            		copyfile(new File(RESOURCE_DIR, type + ".fla"),
            				new File(dir, type + ".fla"));
            	}
	        }
	        stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    private static File dirForTest(Map<String, Object> map) throws IOException {
		String path = String.format( "as%d/swf%d/%s/%s", map.get(ACTIONSCRIPT),
				map.get(FLASH), map.get(PLAYER), map.get(TYPE));
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
		PrintWriter writer = new PrintWriter(file);
		writer.write(script);
		writer.flush();
		writer.close();
	}

    private static String contentsOfFile(File file)
    		throws FileNotFoundException, IOException {
        String script = "";
        byte[] fileIn = new byte[(int)file.length()];
        FileInputStream fileContents = new FileInputStream(file);
        fileContents.read(fileIn);
        script = new String(fileIn);
        fileContents.close();
        return script;
    }

    private static void copyfile(File src, File dest) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dest);

        byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0){
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
    }
}
