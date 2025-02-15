FILES=$( mktemp )

find src/main/java -type f > $FILES

$JAVA_HOME/bin/javac -sourcepath src/main/java -d target/classes @$FILES

find target/classes -type f -printf "%P\n" > $FILES

rm $FILES ; unset FILES

#$JAVA_HOME/bin/java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target/svpn-cookie-cli.jar <url> <user> <password> <pin>

cp -r src/main/resources/* target/classes

rm native-image-build.html
$JAVA_HOME/bin/native-image --emit build-report=native-image-build.html -o svpn-cookie-cli --gc=epsilon -R:MaxHeapSize=16m --static --libc=musl -march=native --enable-sbom --link-at-build-time --exact-reachability-metadata --initialize-at-build-time=io.github.edwgiz.openfortivpn.svpncookie.cli.App --class-path=target/classes io.github.edwgiz.openfortivpn.svpncookie.cli.App


#$JAVA_HOME/bin/native-image --pgo-instrument --gc=serial --link-at-build-time --exact-reachability-metadata -march=native -jar target/svpn-cookie-cli.jar
#./svpn-cookie-cli
#$JAVA_HOME/bin/native-image --pgo --gc=serial --link-at-build-time --exact-reachability-metadata -march=native -jar target/svpn-cookie-cli.jar

#$JAVA_HOME/bin/native-image -3 --gc=serial --static --libc=musl --link-at-build-time --exact-reachability-metadata -march=native -jar target/svpn-cookie-cli.jar -R:MaxHeapSize=64m
