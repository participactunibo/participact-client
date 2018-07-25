 #!/bin/bash

OUT_PROTO=/Users/Andrea/workspaces/workspace_android/participact-client/src/

for f in *.proto;
do
protoc -I=. --java_out=$OUT_PROTO ./$f
done


//protoc -I=. --java_out=/Users/Andrea/workspaces/workspace_android/participact-client/src/ ./file.proto