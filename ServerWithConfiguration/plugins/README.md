To copy plugins listed in `pom.xml` into this directory run:

```
mvn dependency:copy-dependencies
```

To get list of all `*.jar` files in clipboard run the following in this directory:

```
ls *.jar | sed -e 's/^/\"/;s/$/\",/' | xclip -selection c
```
