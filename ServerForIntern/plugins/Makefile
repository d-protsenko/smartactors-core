POM=plugins-pom.xml

all: download clean status

download:
	@echo "### Downloading dependencies for ${POM}"
	mvn -f ${POM} dependency:copy-dependencies

clean:
	@echo "### Removing unnecessary jars"
	@/bin/bash -c "comm -1 -3 <(cat plugins.list | sort | uniq) <(ls -1 *.jar | sort) | xargs rm"

status:
	@echo "### Listing missing jars"
	@/bin/bash -c "comm -2 -3 <(cat plugins.list | sort | uniq) <(ls -1 *.jar | sort)"
