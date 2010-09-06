The files in these directories are templates, written in YAML, that are used to generate the actionscript files that are used to test the compiler.

Each file contains a list of tests. For each test there is refid attribute that is used to refer to an entry in the language reference YAML files that describe the language feature. Each entry in the language reference YAML file has a list of profiles which describes the versions of actionscript, flash and runtime platform that the tesxt is valid for. At least one script containing the test code will be generated for each profile. That ensures that a given feature is tested for every Flash version it is valid for so different forms of code generation can be tested, for example strict equals, ===, has a new action added in Flash 7 to support it. For earlier versions the behaviour was simulated using a more complex set of actions.

There is also an optional list of parameters that defines a set of key-value pairs, which are used to replace tokens, e.g. %value% in the script. A new script is generated for each entry in the parameters list. This allows a large number of variations of parameters to be concisely coded without having to write separate tests.

The scripts are generated using the ActionscriptGenerator class in the src/test/java/tools directory. The YAML file is processed according to the following algorithm:

If there is a set of parameters given:

foreach test  
  foreach profile
    foreach parameter
      replace tokens
      write script
      
If there are no parameters given:

foreach test
  foreach profile
    write script
      
In order to minimise the amount of text in each file, primarily to make maintenance as simple as possible there are a number of rules that the Actionscript generator uses to infer values:

script type: the script is examined to determine whether it is an event handler or a simple script. First, if the script starts with "onClipEvent" it treated as an event handler for a movieclip; then if it starts with "on" it is treated as an event handler for a button; otherwise the script is assumed to be simple script - one that is added to a DoAction object for example. 

script filename: each test has a "file" field which specifies the name of the file where the generated actionscript will be written. This field may either be at the top level of the test or specified in each entry in the parameter list.

For test with no parameters, the file name used to generate the script is specified at the top level:

- test: ...
  file: script.as
  script: ...

For tests with parameters, the file name used to generate the script is specified in each parameter set:

- test: ...
  parameters:
    - ...
      file: set1.as
    - ... 
      file: set2.as
  script: ...
  
There is a variation on this which allows the file field to be omitted from the sets of parameters. The file may be specified at the top level of the test and it will be used as a format string with the parameter set number used to generate a unique filename for each set of parameters - avoiding the scripts from overwriting each other:

- test: ...
  file: script_%d.as
  parameters:
    - ...
    - ... 
  script: ...

In a further twist is it possible to omit the "file" field completely. Each test or set of parameters has a "refid" field which is used to determine coverage of the tests against the actionscript reference YAML files. If the "file" field is missing then the value of "refid"  is used as for script name with an ".as" extension added:

- test: ...
  refid: movieclip_gotoframe
  script: ...

The script generated will be: movieclip_gotoframe.as

