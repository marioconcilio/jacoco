# Build only Jaguar Core and its dependencies
build_core:
	./mvnw install -pl org.jacoco.core -am

# Build all modules
build:
	./mvnw clean install