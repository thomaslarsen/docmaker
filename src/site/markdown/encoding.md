# Overview

Maven plugins that process plain text files depend on knowing the encoding of that file. Differences in encoding between environments can cause incorrect or failing decoding. Maven builds should not be platform dependent. Plugins need to be told the encoding that they should be using in order to read those files.

Maven has a standard convention for these situations, which has been implemented for this plugin.
	
## Specifying encoding

Maven encoding should be set in the project's appropriate POM file. Usually this will be the parent POM.

Encoding is set by adding the following property:

    <project>
      ...
      <properties>
    	<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      </properties>
      ...
    </project>

This will set the source file encoding as UTF-8. Docmaker will then use this specified encoding (provided it is valid) whenever it reads files.

## Unspecified encoding

As per Maven conventions, if no explicit file encoding has not been specified in the appropriate POM's, Docmaker will use the platform default encoding.

Since usage of the platform encoding yields platform-dependent and hence potentially irreproducible builds, Docmaker will print the following warning to inform the user:

    [WARNING] "Using platform encoding ([platformEncoding] actually) to read doc files, i.e. build is platform dependent!