rm -r bin
mkdir bin
javac -cp ./lib/* $(find ./src/* | grep .java) -d bin