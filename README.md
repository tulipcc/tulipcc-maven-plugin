ph-javacc-maven-plugin
======================

An updated version of the [javacc-maven-plugin](http://mojo.codehaus.org/javacc-maven-plugin/) using JavaCC 6.1.3.
The parameters etc. where not touched, so it should be a drop-in replacement for the old javacc-maven-plugin 2.6.

I'm using it e.g. in [ph-css](https://github.com/phax/ph-css) for CSS parsing as well as in [ph-json](https://github.com/phax/ph-json) to build the JSON grammar parser.

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

# Developer info

  * To run the integration tests use the following commandline `mvn test -Dit=true`

---

On Twitter: <a href="https://twitter.com/philiphelger">Follow @philiphelger</a>
  