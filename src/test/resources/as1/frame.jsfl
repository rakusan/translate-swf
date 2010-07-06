var doc = fl.openDocument("file:///../../../frame.fla");
doc.importPublishProfile("file:///../publish.xml");
doc.currentPublishProfile = "compile";

var folder = "file:///.";
var fileMask = ".as";
var files = new Array();

listFiles(folder, fileMask, files);

var script;
var dirOut;
var fileOut;

for (i in files) {
    script = files[i].substring(folder.length + 1);
    fl.actionsPanel.setText("#include \"" + script + "\"");
	fileOut = "../../compiled/" + script.slice(0, -fileMask.length) + ".swf";
	dirOut = "file:///" + fileOut.substring(0, fileOut.lastIndexOf("/"));
	if (!FLfile.exists(dirOut)){
		FLfile.createFolder(dirOut);
	}
    doc.exportSWF("file:///" + fileOut, true);
}

doc.revert();
doc.close();
fl.quit();

function listFiles(dir, mask, files) {
	var list = FLfile.listFolder(dir);
	var path;
	var attr;
	
	for (i in list) {
	    path = dir + "/" + list[i];
		if (FLfile.exists(path)){
			attr = FLfile.getAttributes(path);
			if (attr && (attr.indexOf("D") != -1)) {
				listFiles(path, mask, files);
			} else {
			    if (path.slice(-mask.length) == mask) {
					files.push(path);
				}
			}
		}
	}
}


