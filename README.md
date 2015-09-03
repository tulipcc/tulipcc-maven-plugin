#ph-javacc-maven-plugin
An updated version of the [javacc-maven-plugin](https://github.com/mojohaus/javacc-maven-plugin) using JavaCC 6.1.3.
The parameters etc. where not touched, so it should be a drop-in replacement for the old javacc-maven-plugin 2.6.
It requires Java 1.5 or higher and Apache Maven 2.x or higher. It is licensed under the Apache 2 license.

I'm using it e.g. in [ph-css](https://github.com/phax/ph-css) for CSS parsing.

# Maven usage
Example with 3 executions (two javacc and one jjtree-javacc):

```xml
    <build>
      ...
      <plugin>
        <groupId>com.helger.maven</groupId>
        <artifactId>ph-javacc-maven-plugin</artifactId>
        <version>2.8.0</version>
        <executions>
          <execution>
            <id>jjc1</id>
            <phase>generate-sources</phase>
            <goals>
              <goal>javacc</goal>
            </goals>
            <configuration>
              <jdkVersion>1.5</jdkVersion>
              <javadocFriendlyComments>true</javadocFriendlyComments>
              <packageName>org.javacc.parser</packageName>
              <sourceDirectory>src/main/java/org/javacc/parser</sourceDirectory>
              <outputDirectory>${project.build.directory}/generated-sources/javacc1</outputDirectory>
            </configuration>
          </execution>
          <execution>
            <id>jjt2</id>
            <phase>generate-sources</phase>
            <goals>
              <goal>javacc</goal>
            </goals>
            <configuration>
              <jdkVersion>1.5</jdkVersion>
              <javadocFriendlyComments>true</javadocFriendlyComments>
              <packageName>org.javacc.utils</packageName>
              <sourceDirectory>src/main/java/org/javacc/utils</sourceDirectory>
              <outputDirectory>${project.build.directory}/generated-sources/javacc2</outputDirectory>
            </configuration>
          </execution>
          <execution>
            <id>jjt1</id>
            <phase>generate-sources</phase>
            <goals>
              <goal>jjtree-javacc</goal>
            </goals>
            <configuration>
              <jdkVersion>1.5</jdkVersion>
              <javadocFriendlyComments>true</javadocFriendlyComments>
              <packageName>org.javacc.parser</packageName>
              <sourceDirectory>src/main/java/org/javacc/jjtree</sourceDirectory>
              <excludes>
                <exclude>**/parser/**</exclude>
              </excludes>
              <outputDirectory>${project.build.directory}/generated-sources/jjtree1</outputDirectory>
            </configuration>
          </execution>
        </executions>
      </plugin>
      ...
    </build>
```

Supported goals with the respective parameters are:
  * `javacc` Parses a JavaCC grammar file (`*.jj`) and transforms it to Java source files. Detailed information about the JavaCC options can be found on the [JavaCC website](https://javacc.dev.java.net/)
  * `jjdoc` [JJDoc](https://javacc.dev.java.net/doc/JJDoc.html) takes a JavaCC parser specification and produces documentation for the BNF grammar. This mojo will search the source directory for all `*.jj` files and run JJDoc once for each file it finds. Each of these output files, along with an `index.html` file will be placed in the site directory (`target/site/jjdoc`), and a link will be created in the "Project Reports" menu of the generated site.
  * `jjtree-javacc` Preprocesses decorated grammar files (`*.jjt`) with JJTree and passes the output to JavaCC in order to finally generate a parser with parse tree actions.
  * `jjtree` Parses a JJTree grammar file (`*.jjt`) and transforms it to Java source files and a JavaCC grammar file. Please see the [JJTree Reference Documentation](https://javacc.dev.java.net/doc/JJTree.html) for more information.
  * `jtb-javacc` Preprocesses ordinary grammar files (`*.jtb`) with JTB and passes the output to JavaCC in order to finally generate a parser with parse tree actions.
  * `jtb` Parses a JTB file and transforms it into source files for an AST and a JavaCC grammar file which automatically builds the AST.

# Developer info

  * To run the integration tests use the following commandline `mvn test -Dit=true`

---

On Twitter: <a href="https://twitter.com/philiphelger">Follow @philiphelger</a>
  